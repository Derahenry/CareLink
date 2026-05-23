package com.carelink.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.carelink.data.DatabaseHelper
import com.carelink.data.dao.AuditDao
import com.carelink.data.dao.RequestDao
import com.carelink.data.model.AuditAction
import com.carelink.data.model.AuditEntry
import com.carelink.data.model.RequestStatus
import com.carelink.data.model.WelfareRequest
import com.carelink.util.SessionManager
import com.carelink.data.dao.VisitDao
import com.carelink.data.model.VisitOutcome

// ─────────────────────────────────────────────────────────────────────────────
// RequestViewModel — manages all welfare request business logic
// Sits between the UI screens and the DAO layer (MVVM pattern)
// UI observes state variables here using mutableStateOf (from module slides)
// ─────────────────────────────────────────────────────────────────────────────
class RequestViewModel(application: Application) : AndroidViewModel(application) {

    private val db         = DatabaseHelper(application).writableDatabase
    private val requestDao = RequestDao(db)
    private val auditDao   = AuditDao(db)
    private val session    = SessionManager(application)

    // ── Observable state — UI recomposes when these change ────────────────
    var requests      by mutableStateOf<List<WelfareRequest>>(emptyList())
    var selectedRequest by mutableStateOf<WelfareRequest?>(null)
    var errorMessage  by mutableStateOf("")
    var isLoading     by mutableStateOf(false)

    // ── Filter state for coordinator inbox ────────────────────────────────
    var statusFilter   by mutableStateOf("ALL")
    var priorityFilter by mutableStateOf("ALL")

    // ─────────────────────────────────────────────────────────────────────────
    // loadAllRequests — used by coordinator inbox
    // ─────────────────────────────────────────────────────────────────────────
    fun loadAllRequests() {
        requests = requestDao.getAll(
            statusFilter   = statusFilter.takeIf { it != "ALL" },
            priorityFilter = priorityFilter.takeIf { it != "ALL" }
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadMyRequests — used by resident to see their own requests
    // ─────────────────────────────────────────────────────────────────────────
    fun loadMyRequests() {
        requests = requestDao.getByResident(session.getUserId())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadAssignedVisits — used by support worker
    // ─────────────────────────────────────────────────────────────────────────
    fun loadAssignedVisits() {
        requests = requestDao.getAssignedTo(session.getUserId())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadCompletedForReview — used by safeguarding reviewer
    // ─────────────────────────────────────────────────────────────────────────
    fun loadCompletedForReview() {
        requests = requestDao.getCompletedForReview()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // loadOverdue — additional feature B: overdue queue
    // ─────────────────────────────────────────────────────────────────────────
    fun loadOverdue() {
        requests = requestDao.getOverdue()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // selectRequest — loads a single request into selectedRequest state
    // ─────────────────────────────────────────────────────────────────────────
    fun selectRequest(requestId: Int) {
        selectedRequest = requestDao.getById(requestId)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // submitRequest — resident submits a draft request
    // Creates the request and immediately submits it (or saves as draft first)
    // ─────────────────────────────────────────────────────────────────────────
    fun submitRequest(
        title: String,
        description: String,
        category: String,
        address: String,
        notes: String,
        asDraft: Boolean = false
    ): Boolean {
        if (title.isBlank() || description.isBlank() || address.isBlank()) {
            errorMessage = "Please fill in all required fields"
            return false
        }

        val status = if (asDraft) RequestStatus.DRAFT else RequestStatus.SUBMITTED
        val now    = System.currentTimeMillis()

        val request = WelfareRequest(
            residentId  = session.getUserId(),
            title       = title.trim(),
            description = description.trim(),
            category    = category,
            address     = address.trim(),
            notes       = notes.trim(),
            status      = status,
            createdAt   = now,
            updatedAt   = now
        )

        val newId = requestDao.insert(request)
        if (newId == -1L) {
            errorMessage = "Failed to save request"
            return false
        }

        // Log to audit trail
        auditDao.insert(AuditEntry(
            requestId = newId.toInt(),
            actorId   = session.getUserId(),
            action    = if (asDraft) AuditAction.CREATED else AuditAction.SUBMITTED,
            detail    = "Request created by ${session.getUserName()}",
            timestamp = now
        ))

        loadMyRequests()
        return true
    }

    // ─────────────────────────────────────────────────────────────────────────
    // assignRequest — coordinator assigns request to worker
    // ─────────────────────────────────────────────────────────────────────────
    fun assignRequest(
        requestId: Int,
        workerId: Int,
        priority: String,
        deadlineTs: Long,
        coordinatorNotes: String
    ): Boolean {
        val success = requestDao.assign(requestId, workerId, priority, deadlineTs, coordinatorNotes)
        if (success) {
            auditDao.insert(AuditEntry(
                requestId = requestId,
                actorId   = session.getUserId(),
                action    = AuditAction.ASSIGNED,
                detail    = "Assigned to worker ID $workerId | Priority: $priority",
                timestamp = System.currentTimeMillis()
            ))
            loadAllRequests()
        }
        return success
    }

    // ─────────────────────────────────────────────────────────────────────────
    // completeVisit — support worker marks visit as completed
    // ─────────────────────────────────────────────────────────────────────────
    fun completeVisit(requestId: Int, outcomeNotes: String): Boolean {
        if (outcomeNotes.isBlank()) {
            errorMessage = "Outcome notes are required before completing a visit"
            return false
        }

        val visitDao = VisitDao(db)
        val now = System.currentTimeMillis()

        // Save outcome to visit_outcomes table
        visitDao.insert(VisitOutcome(
            requestId   = requestId,
            workerId    = session.getUserId(),
            notes       = outcomeNotes,
            completedAt = now
        ))

        // Update request status
        val success = requestDao.updateStatus(requestId, RequestStatus.VISIT_COMPLETED)
        if (success) {
            auditDao.insert(AuditEntry(
                requestId = requestId,
                actorId   = session.getUserId(),
                action    = AuditAction.COMPLETED,
                detail    = "Visit completed. Notes: $outcomeNotes",
                timestamp = now
            ))
            loadAssignedVisits()
        }
        return success
    }

    // ─────────────────────────────────────────────────────────────────────────
    // verifyOutcome — reviewer verifies the visit outcome
    // ─────────────────────────────────────────────────────────────────────────
    fun verifyOutcome(requestId: Int, notes: String): Boolean {
        val success = requestDao.updateStatus(requestId, RequestStatus.VERIFIED)
        if (success) {
            auditDao.insert(AuditEntry(
                requestId = requestId,
                actorId   = session.getUserId(),
                action    = AuditAction.VERIFIED,
                detail    = "Verified by ${session.getUserName()}. Notes: $notes",
                timestamp = System.currentTimeMillis()
            ))
            loadCompletedForReview()
        }
        return success
    }

    // ─────────────────────────────────────────────────────────────────────────
    // escalateCase — reviewer escalates the case with a reason
    // ─────────────────────────────────────────────────────────────────────────
    fun escalateCase(requestId: Int, reason: String, notes: String): Boolean {
        val success = requestDao.updateStatus(requestId, RequestStatus.ESCALATED)
        if (success) {
            auditDao.insert(AuditEntry(
                requestId = requestId,
                actorId   = session.getUserId(),
                action    = AuditAction.ESCALATED,
                detail    = "Escalated. Reason: $reason. Notes: $notes",
                timestamp = System.currentTimeMillis()
            ))
            loadCompletedForReview()
        }
        return success
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAuditLog — returns audit entries for a request (for status timeline)
    // ─────────────────────────────────────────────────────────────────────────
    fun getAuditLog(requestId: Int) = auditDao.getByRequest(requestId)
}