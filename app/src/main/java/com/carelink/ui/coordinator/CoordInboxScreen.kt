package com.carelink.ui.coordinator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carelink.data.model.WelfareRequest
import com.carelink.navigation.Routes
import com.carelink.util.DeadlineUtils
import com.carelink.util.SessionManager
import com.carelink.viewmodel.RequestViewModel
import com.carelink.ui.theme.ThemeManager

@Composable
fun CoordInboxScreen(navController: NavHostController) {
    val viewModel: RequestViewModel = viewModel()
    val context = LocalContext.current
    val session = SessionManager(context)

    LaunchedEffect(Unit) { viewModel.loadAllRequests() }
    LaunchedEffect(viewModel.statusFilter, viewModel.priorityFilter) {
        viewModel.loadAllRequests()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top bar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = "Coordinator Inbox",
                    color      = Color.White,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "Welcome, ${session.getUserName()}",
                    color    = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
            Row {
                TextButton(onClick = { navController.navigate(Routes.OVERDUE_QUEUE) }) {
                    Text("Overdue", color = Color.White)
                }
                TextButton(onClick = {
                    SessionManager(context).clearSession()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                })

                {
                    // Dark mode toggle (additional feature E)
                    IconButton(onClick = { ThemeManager.isDarkMode = !ThemeManager.isDarkMode }) {
                        Text(
                            text     = if (ThemeManager.isDarkMode) "☀️" else "🌙",
                            fontSize = 16.sp
                        )
                    }
                    Text("Logout", color = Color.White)
                }
            }
        }

        // ── Filter row ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "SUBMITTED", "UNDER_REVIEW", "ASSIGNED").forEach { status ->
                FilterChip(
                    selected = viewModel.statusFilter == status,
                    onClick  = { viewModel.statusFilter = status },
                    label    = { Text(status.replace("_", " "), fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "HIGH", "MEDIUM", "LOW").forEach { priority ->
                FilterChip(
                    selected = viewModel.priorityFilter == priority,
                    onClick  = { viewModel.priorityFilter = priority },
                    label    = { Text(priority, fontSize = 11.sp) }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // ── Request list ──────────────────────────────────────────────────
        if (viewModel.requests.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "No requests found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.requests) { request ->
                    RequestCard(
                        request = request,
                        onClick = {
                            navController.navigate(Routes.assignScreen(request.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCard(request: WelfareRequest, onClick: () -> Unit) {
    val isOverdue     = DeadlineUtils.isOverdue(request.deadlineTs)
    val deadlineLabel = DeadlineUtils.getDeadlineLabel(request.deadlineTs)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (request.priority.isNotEmpty()) {
                    PriorityBadge(priority = request.priority)
                }
                Text(
                    text       = deadlineLabel,
                    fontSize   = 12.sp,
                    color      = if (isOverdue)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text       = request.title,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp
            )
            Text(
                text     = request.address,
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBadge(status = request.status)
                Text(
                    text     = request.category.replace("_", " "),
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val color = when (priority) {
        "HIGH"   -> Color(0xFFD32F2F)
        "MEDIUM" -> Color(0xFFF57C00)
        else     -> Color(0xFF388E3C)
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            text     = priority,
            color    = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "SUBMITTED"       -> Color(0xFF1565C0)
        "UNDER_REVIEW"    -> Color(0xFFF57C00)
        "ASSIGNED"        -> Color(0xFF6A1B9A)
        "VISIT_COMPLETED" -> Color(0xFF00695C)
        "VERIFIED"        -> Color(0xFF2E7D32)
        "ESCALATED"       -> Color(0xFFD32F2F)
        else              -> Color(0xFF757575)
    }
    Surface(color = color, shape = MaterialTheme.shapes.small) {
        Text(
            text     = status.replace("_", " "),
            color    = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}