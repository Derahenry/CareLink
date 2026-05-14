package com.carelink.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.carelink.data.model.UserRole
import com.carelink.ui.auth.LoginScreen
import com.carelink.ui.auth.RegisterScreen
import com.carelink.util.SessionManager
import com.carelink.ui.coordinator.CoordInboxScreen

object Routes {
    const val LOGIN          = "login"
    const val REGISTER       = "register"
    const val RESIDENT_HOME  = "resident_home"
    const val NEW_REQUEST    = "new_request"
    const val REQUEST_DETAIL = "request_detail/{requestId}"
    const val COORD_INBOX    = "coord_inbox"
    const val ASSIGN_SCREEN  = "assign_screen/{requestId}"
    const val OVERDUE_QUEUE  = "overdue_queue"
    const val WORKER_VISITS  = "worker_visits"
    const val COMPLETE_VISIT = "complete_visit/{requestId}"
    const val REVIEW_QUEUE   = "review_queue"
    const val REVIEW_DETAIL  = "review_detail/{requestId}"
    const val AUDIT_LOG      = "audit_log"

    fun requestDetail(requestId: Int) = "request_detail/$requestId"
    fun assignScreen(requestId: Int)  = "assign_screen/$requestId"
    fun completeVisit(requestId: Int) = "complete_visit/$requestId"
    fun reviewDetail(requestId: Int)  = "review_detail/$requestId"
}

fun getRoleHome(role: String): String = when (role) {
    UserRole.COORDINATOR -> Routes.COORD_INBOX
    UserRole.WORKER      -> Routes.WORKER_VISITS
    UserRole.REVIEWER    -> Routes.REVIEW_QUEUE
    else                 -> Routes.RESIDENT_HOME
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "🚧 $name — coming soon")
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    val startDestination = if (sessionManager.isLoggedIn()) {
        getRoleHome(sessionManager.getUserRole())
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        // ── Auth screens — now real, not placeholders ──────────────────
        composable(Routes.LOGIN)    { LoginScreen(navController) }
        composable(Routes.REGISTER) { RegisterScreen(navController) }

        // ── Remaining screens — still placeholders for now ─────────────
        composable(Routes.RESIDENT_HOME)  { PlaceholderScreen("Resident Home") }
        composable(Routes.NEW_REQUEST)    { PlaceholderScreen("New Request") }
        composable(Routes.REQUEST_DETAIL) { PlaceholderScreen("Request Detail") }
        composable(Routes.COORD_INBOX) { CoordInboxScreen(navController) }
        composable(Routes.ASSIGN_SCREEN)  { PlaceholderScreen("Assign Screen") }
        composable(Routes.OVERDUE_QUEUE)  { PlaceholderScreen("Overdue Queue") }
        composable(Routes.WORKER_VISITS)  { PlaceholderScreen("Worker Visits") }
        composable(Routes.COMPLETE_VISIT) { PlaceholderScreen("Complete Visit") }
        composable(Routes.REVIEW_QUEUE)   { PlaceholderScreen("Review Queue") }
        composable(Routes.REVIEW_DETAIL)  { PlaceholderScreen("Review Detail") }
        composable(Routes.AUDIT_LOG)      { PlaceholderScreen("Audit Log") }
    }
}