package com.carelink.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.carelink.data.model.UserRole

// ─────────────────────────────────────────────────────────────────────────────
// DatabaseHelper — extends SQLiteOpenHelper (taught in module slides)
// This class is responsible for creating and managing our local SQLite database.
// All 5 tables are created here. Indexes are added for performance (NFR 8).
// ─────────────────────────────────────────────────────────────────────────────
class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "carelink.db"
        const val DATABASE_VERSION = 1

        // --- Table names ---
        const val TABLE_USERS         = "users"
        const val TABLE_REQUESTS      = "requests"
        const val TABLE_VISIT_OUTCOMES = "visit_outcomes"
        const val TABLE_REVIEWS       = "reviews"
        const val TABLE_AUDIT_LOG     = "audit_log"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // onCreate — called once when the database is first created on the device.
    // This is where we define all our tables using SQL CREATE TABLE statements.
    // ─────────────────────────────────────────────────────────────────────────
    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        createIndexes(db)
        seedStaffAccounts(db)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // onUpgrade — called when DATABASE_VERSION increases.
    // For now we drop and recreate. In production you'd migrate data instead.
    // ─────────────────────────────────────────────────────────────────────────
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AUDIT_LOG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REVIEWS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VISIT_OUTCOMES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REQUESTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createTables — each table matches our schema from the project plan
    // ─────────────────────────────────────────────────────────────────────────
    private fun createTables(db: SQLiteDatabase) {

        // TABLE: users — stores all app users (residents, staff)
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                email       TEXT NOT NULL UNIQUE,
                password    TEXT NOT NULL,
                full_name   TEXT NOT NULL,
                role        TEXT NOT NULL
            )
        """.trimIndent())

        // TABLE: requests — the core welfare check request
        db.execSQL("""
            CREATE TABLE $TABLE_REQUESTS (
                id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                resident_id         INTEGER NOT NULL,
                title               TEXT NOT NULL,
                description         TEXT NOT NULL,
                category            TEXT NOT NULL,
                address             TEXT NOT NULL,
                notes               TEXT DEFAULT '',
                status              TEXT NOT NULL DEFAULT 'DRAFT',
                priority            TEXT DEFAULT '',
                deadline_ts         INTEGER,
                assigned_to         INTEGER,
                group_id            INTEGER,
                coordinator_notes   TEXT DEFAULT '',
                created_at          INTEGER NOT NULL,
                updated_at          INTEGER NOT NULL,
                FOREIGN KEY(resident_id) REFERENCES $TABLE_USERS(id),
                FOREIGN KEY(assigned_to) REFERENCES $TABLE_USERS(id)
            )
        """.trimIndent())

        // TABLE: visit_outcomes — recorded when worker completes a visit
        db.execSQL("""
            CREATE TABLE $TABLE_VISIT_OUTCOMES (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                request_id      INTEGER NOT NULL UNIQUE,
                worker_id       INTEGER NOT NULL,
                notes           TEXT NOT NULL,
                started_at      INTEGER,
                completed_at    INTEGER NOT NULL,
                FOREIGN KEY(request_id) REFERENCES $TABLE_REQUESTS(id),
                FOREIGN KEY(worker_id)  REFERENCES $TABLE_USERS(id)
            )
        """.trimIndent())

        // TABLE: reviews — recorded by safeguarding reviewer
        db.execSQL("""
            CREATE TABLE $TABLE_REVIEWS (
                id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                request_id          INTEGER NOT NULL UNIQUE,
                reviewer_id         INTEGER NOT NULL,
                outcome             TEXT NOT NULL,
                escalation_reason   TEXT DEFAULT '',
                notes               TEXT DEFAULT '',
                reviewed_at         INTEGER NOT NULL,
                FOREIGN KEY(request_id)  REFERENCES $TABLE_REQUESTS(id),
                FOREIGN KEY(reviewer_id) REFERENCES $TABLE_USERS(id)
            )
        """.trimIndent())

        // TABLE: audit_log — every key action is recorded here (governance requirement)
        db.execSQL("""
            CREATE TABLE $TABLE_AUDIT_LOG (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                request_id  INTEGER NOT NULL,
                actor_id    INTEGER NOT NULL,
                `action`    TEXT NOT NULL,
                detail      TEXT DEFAULT '',
                timestamp   INTEGER NOT NULL,
                FOREIGN KEY(request_id) REFERENCES $TABLE_REQUESTS(id),
                FOREIGN KEY(actor_id)   REFERENCES $TABLE_USERS(id)
            )
        """.trimIndent())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createIndexes — speeds up filtered queries (satisfies NFR 8: performance)
    // ─────────────────────────────────────────────────────────────────────────
    private fun createIndexes(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX idx_requests_status   ON $TABLE_REQUESTS(status)")
        db.execSQL("CREATE INDEX idx_requests_deadline ON $TABLE_REQUESTS(deadline_ts)")
        db.execSQL("CREATE INDEX idx_requests_assigned ON $TABLE_REQUESTS(assigned_to)")
        db.execSQL("CREATE INDEX idx_audit_request     ON $TABLE_AUDIT_LOG(request_id)")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // seedStaffAccounts — pre-loads staff accounts so coordinators, workers,
    // and reviewers can log in immediately without self-registering.
    // Residents register themselves via the Register screen.
    // ─────────────────────────────────────────────────────────────────────────
    private fun seedStaffAccounts(db: SQLiteDatabase) {
        val staffAccounts = listOf(
            Triple("coordinator@carelink.com",  "password123", "Jane Cooper")   to UserRole.COORDINATOR,
            Triple("worker1@carelink.com",      "password123", "Tom Harris")    to UserRole.WORKER,
            Triple("worker2@carelink.com",      "password123", "Amy Singh")     to UserRole.WORKER,
            Triple("reviewer@carelink.com",     "password123", "Dr. Paul Webb") to UserRole.REVIEWER
        )

        for ((credentials, role) in staffAccounts) {
            val (email, password, name) = credentials
            db.execSQL("""
                INSERT INTO $TABLE_USERS (email, password, full_name, role)
                VALUES ('$email', '$password', '$name', '$role')
            """.trimIndent())
        }
    }
}