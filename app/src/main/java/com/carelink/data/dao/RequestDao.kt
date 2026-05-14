package com.carelink.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.carelink.data.DatabaseHelper
import com.carelink.data.model.WelfareRequest

// ─────────────────────────────────────────────────────────────────────────────
// RequestDao — all database operations for welfare requests
// Uses raw SQLite as taught in module slides (rawQuery, ContentValues, Cursor)
// ─────────────────────────────────────────────────────────────────────────────
class RequestDao(private val db: SQLiteDatabase) {

    // ─────────────────────────────────────────────────────────────────────────
    // insert — creates a new welfare request (status = DRAFT)
    // Returns the new row ID, or -1 if it failed
    // ─────────────────────────────────────────────────────────────────────────
    fun insert(request: WelfareRequest): Long {
        val values = ContentValues().apply {
            put("resident_id",       request.residentId)
            put("title",             request.title)
            put("description",       request.description)
            put("category",          request.category)
            put("address",           request.address)
            put("notes",             request.notes)
            put("status",            request.status)
            put("priority",          request.priority)
            put("deadline_ts",       request.deadlineTs)
            put("assigned_to",       request.assignedTo)
            put("coordinator_notes", request.coordinatorNotes)
            put("created_at",        request.createdAt)
            put("updated_at",        request.updatedAt)
        }
        return db.insert(DatabaseHelper.TABLE_REQUESTS, null, values)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getById — fetch a single request by its ID
    // ─────────────────────────────────────────────────────────────────────────
    fun getById(requestId: Int): WelfareRequest? {
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_REQUESTS} WHERE id = ?",
            arrayOf(requestId.toString())
        )
        var request: WelfareRequest? = null
        if (cursor.moveToFirst()) request = cursor.toRequest()
        cursor.close()
        return request
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getByResident — all requests submitted by a specific resident
    // ─────────────────────────────────────────────────────────────────────────
    fun getByResident(residentId: Int): List<WelfareRequest> {
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_REQUESTS} WHERE resident_id = ? ORDER BY created_at DESC",
            arrayOf(residentId.toString())
        )
        return cursor.toRequestList()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAll — all requests for coordinator inbox, with optional filters
    // ─────────────────────────────────────────────────────────────────────────
    fun getAll(
        statusFilter: String? = null,
        priorityFilter: String? = null
    ): List<WelfareRequest> {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (!statusFilter.isNullOrEmpty() && statusFilter != "ALL") {
            conditions.add("status = ?")
            args.add(statusFilter)
        }
        if (!priorityFilter.isNullOrEmpty() && priorityFilter != "ALL") {
            conditions.add("priority = ?")
            args.add(priorityFilter)
        }

        val where = if (conditions.isEmpty()) "" else "WHERE ${conditions.joinToString(" AND ")}"
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_REQUESTS} $where ORDER BY deadline_ts ASC, created_at DESC",
            args.toTypedArray()
        )
        return cursor.toRequestList()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getOverdue — requests past their deadline (additional feature B)
    // ─────────────────────────────────────────────────────────────────────────
    fun getOverdue(): List<WelfareRequest> {
        val now = System.currentTimeMillis()
        val cursor = db.rawQuery(
            """SELECT * FROM ${DatabaseHelper.TABLE_REQUESTS} 
               WHERE deadline_ts IS NOT NULL 
               AND deadline_ts < ? 
               AND status NOT IN ('VERIFIED','ESCALATED')
               ORDER BY deadline_ts ASC""",
            arrayOf(now.toString())
        )
        return cursor.toRequestList()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAssignedTo — requests assigned to a specific support worker
    // ─────────────────────────────────────────────────────────────────────────
    fun getAssignedTo(workerId: Int): List<WelfareRequest> {
        val cursor = db.rawQuery(
            """SELECT * FROM ${DatabaseHelper.TABLE_REQUESTS} 
               WHERE assigned_to = ? AND status = 'ASSIGNED'
               ORDER BY 
                 CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END,
                 deadline_ts ASC""",
            arrayOf(workerId.toString())
        )
        return cursor.toRequestList()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCompletedForReview — visits awaiting safeguarding review
    // ─────────────────────────────────────────────────────────────────────────
    fun getCompletedForReview(): List<WelfareRequest> {
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_REQUESTS} WHERE status = 'VISIT_COMPLETED' ORDER BY updated_at ASC",
            null
        )
        return cursor.toRequestList()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateStatus — changes the status of a request
    // Always call AuditDao.insert() after this to log the change
    // ─────────────────────────────────────────────────────────────────────────
    fun updateStatus(requestId: Int, newStatus: String): Boolean {
        val values = ContentValues().apply {
            put("status",     newStatus)
            put("updated_at", System.currentTimeMillis())
        }
        return db.update(
            DatabaseHelper.TABLE_REQUESTS,
            values,
            "id = ?",
            arrayOf(requestId.toString())
        ) > 0
    }

    // ─────────────────────────────────────────────────────────────────────────
    // assign — coordinator assigns a request to a worker with priority + deadline
    // ─────────────────────────────────────────────────────────────────────────
    fun assign(
        requestId: Int,
        workerId: Int,
        priority: String,
        deadlineTs: Long,
        coordinatorNotes: String
    ): Boolean {
        val values = ContentValues().apply {
            put("assigned_to",       workerId)
            put("priority",          priority)
            put("deadline_ts",       deadlineTs)
            put("coordinator_notes", coordinatorNotes)
            put("status",            "ASSIGNED")
            put("updated_at",        System.currentTimeMillis())
        }
        return db.update(
            DatabaseHelper.TABLE_REQUESTS,
            values,
            "id = ?",
            arrayOf(requestId.toString())
        ) > 0
    }

    // ─────────────────────────────────────────────────────────────────────────
    // delete — only allowed on DRAFT requests (resident can delete their drafts)
    // ─────────────────────────────────────────────────────────────────────────
    fun deleteDraft(requestId: Int, residentId: Int): Boolean {
        return db.delete(
            DatabaseHelper.TABLE_REQUESTS,
            "id = ? AND resident_id = ? AND status = 'DRAFT'",
            arrayOf(requestId.toString(), residentId.toString())
        ) > 0
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cursor extension — converts a Cursor row to a WelfareRequest object
    // This pattern matches the Cursor usage taught in your module slides
    // ─────────────────────────────────────────────────────────────────────────
    private fun android.database.Cursor.toRequest(): WelfareRequest {
        return WelfareRequest(
            id               = getInt(getColumnIndexOrThrow("id")),
            residentId       = getInt(getColumnIndexOrThrow("resident_id")),
            title            = getString(getColumnIndexOrThrow("title")),
            description      = getString(getColumnIndexOrThrow("description")),
            category         = getString(getColumnIndexOrThrow("category")),
            address          = getString(getColumnIndexOrThrow("address")),
            notes            = getString(getColumnIndexOrThrow("notes")) ?: "",
            status           = getString(getColumnIndexOrThrow("status")),
            priority         = getString(getColumnIndexOrThrow("priority")) ?: "",
            deadlineTs       = if (isNull(getColumnIndexOrThrow("deadline_ts"))) null
            else getLong(getColumnIndexOrThrow("deadline_ts")),
            assignedTo       = if (isNull(getColumnIndexOrThrow("assigned_to"))) null
            else getInt(getColumnIndexOrThrow("assigned_to")),
            coordinatorNotes = getString(getColumnIndexOrThrow("coordinator_notes")) ?: "",
            createdAt        = getLong(getColumnIndexOrThrow("created_at")),
            updatedAt        = getLong(getColumnIndexOrThrow("updated_at"))
        )
    }

    private fun android.database.Cursor.toRequestList(): List<WelfareRequest> {
        val list = mutableListOf<WelfareRequest>()
        while (moveToNext()) list.add(toRequest())
        close()
        return list
    }
}