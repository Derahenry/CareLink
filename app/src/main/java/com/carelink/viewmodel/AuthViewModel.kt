package com.carelink.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.carelink.data.DatabaseHelper
import com.carelink.data.model.User
import com.carelink.data.model.UserRole
import com.carelink.util.SessionManager
import android.content.ContentValues


// AuthViewModel — manages login and registration logic
// Extends AndroidViewModel so we can access application context for
// DatabaseHelper and SessionManager (both need a Context to work).
// This matches the ViewModel pattern from your module's State Management slides.

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application)
    private val sessionManager = SessionManager(application)


    // login — checks email + password against the users table
    // Returns the User object if found, null if credentials are wrong

    fun login(email: String, password: String): User? {
        val db = dbHelper.readableDatabase

        // rawQuery matches the SQLite pattern from your module slides
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE email = ? AND password = ?",
            arrayOf(email.trim(), password.trim())
        )

        var user: User? = null

        if (cursor.moveToFirst()) {
            // Read each column by name from the Cursor — as taught in slides
            user = User(
                id       = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                email    = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                role     = cursor.getString(cursor.getColumnIndexOrThrow("role"))
            )
        }

        cursor.close()
        return user
    }


    // register — inserts a new Resident or Carer into the users table
    // Returns true if successful, false if the email already exists // ─────────────────────────────────────────────────────────────────────────
    fun register(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): Boolean {
        // Validate — only residents and carers can self-register
        if (role != UserRole.RESIDENT && role != UserRole.CARER) return false
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) return false

        val db = dbHelper.writableDatabase

        // ContentValues is the standard way to insert data — from your slides
        val values = ContentValues().apply {
            put("email",     email.trim())
            put("password",  password.trim())
            put("full_name", fullName.trim())
            put("role",      role)
        }

        // insert() returns -1 if it fails (e.g. duplicate email due to UNIQUE constraint)
        val result = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        return result != -1L
    }

    // ─────────────────────────────────────────────────────────────────────────
    // saveSession — stores the logged-in user in SharedPreferences
    // ─────────────────────────────────────────────────────────────────────────
    fun saveSession(user: User) {
        sessionManager.saveSession(
            userId    = user.id,
            userName  = user.fullName,
            userRole  = user.role,
            userEmail = user.email
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // logout — clears SharedPreferences session
    // ─────────────────────────────────────────────────────────────────────────
    fun logout() {
        sessionManager.clearSession()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSession — reads current session (used by MainActivity on startup)
    // ─────────────────────────────────────────────────────────────────────────
    fun getSessionManager(): SessionManager = sessionManager
}