package com.carelink.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.carelink.data.DatabaseHelper
import com.carelink.data.model.AuditEntry

// ─────────────────────────────────────────────────────────────────────────────
// AuditDao — handles all audit log database operations
// Every key action in the app writes a row here (governance requirement)
// ─────────────────────────────────────────────────────────────────────────────
class AuditDao(private val db: SQLiteDatabase) {

    // ─────────────────────────────────────────────────────────────────────────
    // insert — logs a key action to the audit trail
    // Called after every status change in the app
    // ─────────────────────────────────────────────────────────────────────────
    fun insert(entry: AuditEntry): Long {
        val values = ContentValues().apply {
            put("request_id", entry.requestId)
            put("actor_id",   entry.actorId)
            put("action",     entry.action)
            put("detail",     entry.detail)
            put("timestamp",  entry.timestamp)
        }
        return db.insert(DatabaseHelper.TABLE_AUDIT_LOG, null, values)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getByRequest — full audit trail for a single request
    // Used in the status timeline composable
    // ─────────────────────────────────────────────────────────────────────────
    fun getByRequest(requestId: Int): List<AuditEntry> {
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_AUDIT_LOG} WHERE request_id = ? ORDER BY timestamp ASC",
            arrayOf(requestId.toString())
        )
        val list = mutableListOf<AuditEntry>()
        while (cursor.moveToNext()) {
            list.add(
                AuditEntry(
                    id        = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    requestId = cursor.getInt(cursor.getColumnIndexOrThrow("request_id")),
                    actorId   = cursor.getInt(cursor.getColumnIndexOrThrow("actor_id")),
                    action    = cursor.getString(cursor.getColumnIndexOrThrow("action")),
                    detail    = cursor.getString(cursor.getColumnIndexOrThrow("detail")) ?: "",
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                )
            )
        }
        cursor.close()
        return list
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAll — full audit log for the audit review screen
    // ─────────────────────────────────────────────────────────────────────────
    fun getAll(): List<AuditEntry> {
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_AUDIT_LOG} ORDER BY timestamp DESC",
            null
        )
        val list = mutableListOf<AuditEntry>()
        while (cursor.moveToNext()) {
            list.add(
                AuditEntry(
                    id        = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    requestId = cursor.getInt(cursor.getColumnIndexOrThrow("request_id")),
                    actorId   = cursor.getInt(cursor.getColumnIndexOrThrow("actor_id")),
                    action    = cursor.getString(cursor.getColumnIndexOrThrow("action")),
                    detail    = cursor.getString(cursor.getColumnIndexOrThrow("detail")) ?: "",
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                )
            )
        }
        cursor.close()
        return list
    }
}