package com.carelink.data.model

// ─────────────────────────────────────────────
// USER — represents anyone who uses the app
// ─────────────────────────────────────────────
data class User(
    val id: Int = 0,
    val email: String,
    val password: String,
    val fullName: String,
    val role: String  // RESIDENT | CARER | COORDINATOR | WORKER | REVIEWER
)

// ─────────────────────────────────────────────
// WELFARE REQUEST — the core entity of the app
// ─────────────────────────────────────────────
data class WelfareRequest(
    val id: Int = 0,
    val residentId: Int,
    val title: String,
    val description: String,
    val category: String,     // ROUTINE | MISSED_CONTACT | WELLBEING
    val address: String,
    val notes: String = "",
    val status: String = "DRAFT",
    val priority: String = "", // LOW | MEDIUM | HIGH
    val deadlineTs: Long? = null,
    val assignedTo: Int? = null,
    val groupId: Int? = null,
    val coordinatorNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// VISIT OUTCOME — recorded by the support worker
// ─────────────────────────────────────────────
data class VisitOutcome(
    val id: Int = 0,
    val requestId: Int,
    val workerId: Int,
    val notes: String,
    val startedAt: Long? = null,
    val completedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// REVIEW — recorded by the safeguarding reviewer
// ─────────────────────────────────────────────
data class Review(
    val id: Int = 0,
    val requestId: Int,
    val reviewerId: Int,
    val outcome: String,          // VERIFIED | ESCALATED
    val escalationReason: String = "",
    val notes: String = "",
    val reviewedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// AUDIT ENTRY — every key action is logged here
// ─────────────────────────────────────────────
data class AuditEntry(
    val id: Int = 0,
    val requestId: Int,
    val actorId: Int,
    val action: String,   // CREATED | SUBMITTED | ASSIGNED | COMPLETED | VERIFIED | ESCALATED
    val detail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// CONSTANTS — used throughout the app
// ─────────────────────────────────────────────
object UserRole {
    const val RESIDENT = "RESIDENT"
    const val CARER = "CARER"
    const val COORDINATOR = "COORDINATOR"
    const val WORKER = "WORKER"
    const val REVIEWER = "REVIEWER"
}

object RequestStatus {
    const val DRAFT = "DRAFT"
    const val SUBMITTED = "SUBMITTED"
    const val UNDER_REVIEW = "UNDER_REVIEW"
    const val ASSIGNED = "ASSIGNED"
    const val VISIT_COMPLETED = "VISIT_COMPLETED"
    const val VERIFIED = "VERIFIED"
    const val ESCALATED = "ESCALATED"
}

object Priority {
    const val LOW = "LOW"
    const val MEDIUM = "MEDIUM"
    const val HIGH = "HIGH"
}

object AuditAction {
    const val CREATED = "CREATED"
    const val SUBMITTED = "SUBMITTED"
    const val ASSIGNED = "ASSIGNED"
    const val COMPLETED = "COMPLETED"
    const val VERIFIED = "VERIFIED"
    const val ESCALATED = "ESCALATED"
}

object Category {
    const val ROUTINE = "ROUTINE"
    const val MISSED_CONTACT = "MISSED_CONTACT"
    const val WELLBEING = "WELLBEING"
}