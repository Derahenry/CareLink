package com.carelink.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.carelink.data.DatabaseHelper
import com.carelink.data.model.VisitOutcome


// VisitDao — handles visit outcome database operations
// Called when a support worker completes a visit

class VisitDao(private val db: SQLiteDatabase) {


    // insert — saves the visit outcome to the database
    // Called alongside updateStatus in RequestViewModel.completeVisit()

    fun insert(outcome: VisitOutcome): Long {
        val values = ContentValues().apply {
            put("request_id",   outcome.requestId)
            put("worker_id",    outcome.workerId)
            put("notes",        outcome.notes)
            put("started_at",   outcome.startedAt)
            put("completed_at", outcome.completedAt)
        }
        return db.insert(DatabaseHelper.TABLE_VISIT_OUTCOMES, null, values)
    }


    // getByRequest — fetch outcome for a specific request
    // Used in ReviewDetailScreen to show worker outcome notes

    fun getByRequest(requestId: Int): VisitOutcome? {
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_VISIT_OUTCOMES} WHERE request_id = ?",
            arrayOf(requestId.toString())
        )
        var outcome: VisitOutcome? = null
        if (cursor.moveToFirst()) {
            outcome = VisitOutcome(
                id          = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                requestId   = cursor.getInt(cursor.getColumnIndexOrThrow("request_id")),
                workerId    = cursor.getInt(cursor.getColumnIndexOrThrow("worker_id")),
                notes       = cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                startedAt   = if (cursor.isNull(cursor.getColumnIndexOrThrow("started_at")))
                    null
                else
                    cursor.getLong(cursor.getColumnIndexOrThrow("started_at")),
                completedAt = cursor.getLong(cursor.getColumnIndexOrThrow("completed_at"))
            )
        }
        cursor.close()
        return outcome
    }


    // getByWorker — all visits completed by a specific worker
    // Can be used for visit history feature

    fun getByWorker(workerId: Int): List<VisitOutcome> {
        val cursor = db.rawQuery(
            """SELECT * FROM ${DatabaseHelper.TABLE_VISIT_OUTCOMES} 
               WHERE worker_id = ? ORDER BY completed_at DESC""",
            arrayOf(workerId.toString())
        )
        val list = mutableListOf<VisitOutcome>()
        while (cursor.moveToNext()) {
            list.add(VisitOutcome(
                id          = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                requestId   = cursor.getInt(cursor.getColumnIndexOrThrow("request_id")),
                workerId    = cursor.getInt(cursor.getColumnIndexOrThrow("worker_id")),
                notes       = cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                startedAt   = if (cursor.isNull(cursor.getColumnIndexOrThrow("started_at")))
                    null
                else
                    cursor.getLong(cursor.getColumnIndexOrThrow("started_at")),
                completedAt = cursor.getLong(cursor.getColumnIndexOrThrow("completed_at"))
            ))
        }
        cursor.close()
        return list
    }
}