package com.carelink.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.carelink.data.DatabaseHelper
import com.carelink.data.dao.AuditDao
import com.carelink.data.dao.RequestDao
import com.carelink.data.dao.VisitDao
import com.carelink.data.model.AuditAction
import com.carelink.data.model.AuditEntry
import com.carelink.data.model.RequestStatus
import com.carelink.data.model.VisitOutcome
import com.carelink.data.model.WelfareRequest
import com.carelink.util.NotificationHelper
import com.carelink.util.SessionManager

class RequestViewModel(application: Application) : AndroidViewModel(application) {

    private val context     = getApplication<Application>()
    private val db          = DatabaseHelper(context).writableDatabase
    private val requestDao  = RequestDao(db)
    private val auditDao    = AuditDao(db)
    private val session     = SessionManager(context)

    var requests        by mutableStateOf<List<WelfareRequest>>(emptyList())
    var selectedRequest by mutableStateOf<WelfareRequest?>(null)
    var statusFilter    by mutableStateOf("ALL")
    var priorityFilter  by mutableStateOf("ALL")
    var errorMessage    by mutableStateOf("")

    // ── Load functions ────────────────────────────────────────────────────────

    fun loadAllRequests() {
        requests = requestDao.getAll(
            statusFilter   = if (statusFilter == "ALL") null else statusFilter,
            priorityFilter = if (priorityFilter == "ALL") null else priorityFilter
        )
    }

    fun loadMyRequests() {
        requests = requestDao.getByResident(session.getUserId())
    }

    fun loadAssignedVisits() {
        requests = requestDao.getAssignedTo(session.getUserId())
    }

    fun loadCompletedForReview() {
        requests = requestDao.getCompletedForReview()
    }

    fun loadOverdue() {
        requests = requestDao.getOverdue()
    }

    fun selectRequest(requestId: Int) {
        selectedRequest = requestDao.getById(requestId)
    }

    fun getAuditLog(requestId: Int): List<AuditEntry> {
        return auditDao.getByRequest(requestId)
    }

    // ── Submit request ────────────────────────────────────────────────────────

    fun submitRequest(
        title: String,
        description: String,
        category: String,
        address: String,
        notes: String,
        isDraft: Boolean
    ): Boolean {
        val now    = System.currentTimeMillis()
        val status = if (isDraft) RequestStatus.DRAFT else RequestStatus.SUBMITTED
        val request = WelfareRequest(
            residentId  = session.getUserId(),
            title       = title,
            description = description,
            category    = category,
            address     = address,
            notes       = notes,
            status      = status,
            createdAt   = now,
            updatedAt   = now
        )
        val id = requestDao.insert(request)
        val success = id != -1L

        if (success && !isDraft) {
            auditDao.insert(AuditEntry(
                requestId = id.toInt(),
                actorId   = session.getUserId(),
                action    = AuditAction.SUBMITTED,
                detail    = "Request submitted by resident",
                timestamp = now
            ))
            NotificationHelper.notifyNewRequest(context, title, category)
        }
        return success
    }

    // ── Assign request ────────────────────────────────────────────────────────

    fun assignRequest(
        requestId: Int,
        workerId: Int,
        workerName: String,
        priority: String,
        deadlineTs: Long,
        coordinatorNotes: String
    ): Boolean {
        val now = System.currentTimeMillis()
        val success = requestDao.assign(
            requestId        = requestId,
            workerId         = workerId,
            priority         = priority,
            deadlineTs       = deadlineTs,
            coordinatorNotes = coordinatorNotes
        )
        if (success) {
            auditDao.insert(AuditEntry(
                requestId = requestId,
                actorId   = session.getUserId(),
                action    = AuditAction.ASSIGNED,
                detail    = "Assigned to $workerName with $priority priority",
                timestamp = now
            ))
            NotificationHelper.notifyVisitAssigned(context, requestId.toString(), workerName)
            loadAllRequests()
        }
        return success
    }

    // ── Complete visit ────────────────────────────────────────────────────────

    fun completeVisit(requestId: Int, outcomeNotes: String): Boolean {
        if (outcomeNotes.isBlank()) {
            errorMessage = "Outcome notes are required before completing a visit"
            return false
        }
        val visitDao = VisitDao(db)
        val now = System.currentTimeMillis()

        visitDao.insert(VisitOutcome(
            requestId   = requestId,
            workerId    = session.getUserId(),
            notes       = outcomeNotes,
            completedAt = now
        ))

        val success = requestDao.updateStatus(requestId, RequestStatus.VISIT_COMPLETED)
        if (success) {
            auditDao.insert(AuditEntry(
                requestId = requestId,
                actorId   = session.getUserId(),
                action    = AuditAction.COMPLETED,
                detail    = "Visit completed. Notes: $outcomeNotes",
                timestamp = now
            ))
            NotificationHelper.notifyVisitCompleted(context, requestId.toString())
            loadAssignedVisits()
        }
        return success
    }

    // ── Verify outcome ────────────────────────────────────────────────────────

    fun verifyOutcome(requestId: Int, reviewNotes: String): Boolean {
        val now     = System.currentTimeMillis()
        val success = requestDao.updateStatus(requestId, RequestStatus.VERIFIED)
        if (success) {
            auditDao.insert(AuditEntry(
                requestId = requestId,
                actorId   = session.getUserId(),
                action    = AuditAction.VERIFIED,
                detail    = if (reviewNotes.isBlank()) "Outcome verified" else reviewNotes,
                timestamp = now
            ))
            NotificationHelper.notifyVerified(context, requestId.toString())
            loadCompletedForReview()
        }
        return success
    }

    // ── Escalate case ─────────────────────────────────────────────────────────

    fun escalateCase(requestId: Int, reason: String, notes: String): Boolean {
        val now     = System.currentTimeMillis()
        val success = requestDao.updateStatus(requestId, RequestStatus.ESCALATED)
        if (success) {
            auditDao.insert(AuditEntry(
                requestId = requestId,
                actorId   = session.getUserId(),
                action    = AuditAction.ESCALATED,
                detail    = "Reason: $reason. Notes: $notes",
                timestamp = now
            ))
            NotificationHelper.notifyEscalated(context, requestId.toString(), reason)
            loadCompletedForReview()
        }
        return success
    }
}