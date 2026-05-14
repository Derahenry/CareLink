package com.carelink.util

import android.content.Context
import android.content.SharedPreferences


// SessionManager — uses SharedPreferences to persist login state across restarts
// This matches the SharedPreferences pattern taught in the module slides.
// When the user logs in, we save their id, name, and role here.
// When the app restarts, we read this to decide which screen to show.

class SessionManager(context: Context) {

    // SharedPreferences is a key-value store — perfect for small session data
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE  // MODE_PRIVATE = only this app can read it
    )

    companion object {
        private const val PREFS_NAME    = "carelink_session"
        private const val KEY_USER_ID   = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_EMAIL = "user_email"
        private const val NO_USER       = -1
    }

    // ─────────────────────────────────────────────────────────────────────────
    // saveSession — called when user successfully logs in
    // ─────────────────────────────────────────────────────────────────────────
    fun saveSession(userId: Int, userName: String, userRole: String, userEmail: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_ROLE, userRole)
            .putString(KEY_USER_EMAIL, userEmail)
            .apply() // apply() saves asynchronously — better than commit() for UI thread
    }

    // ─────────────────────────────────────────────────────────────────────────
    // clearSession — called when user logs out
    // ─────────────────────────────────────────────────────────────────────────
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters — read saved session data
    // ─────────────────────────────────────────────────────────────────────────
    fun isLoggedIn(): Boolean = getUserId() != NO_USER

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, NO_USER)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "") ?: ""

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
}