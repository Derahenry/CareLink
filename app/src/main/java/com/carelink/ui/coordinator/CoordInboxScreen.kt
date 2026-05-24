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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.carelink.ui.theme.CareColors


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

        // ── Top bar ───────────────

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "Inbox",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text     = "Welcome, ${session.getUserName()}",
                    fontSize = 12.5.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Dark mode toggle
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { ThemeManager.isDarkMode = !ThemeManager.isDarkMode }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text     = if (ThemeManager.isDarkMode) "☀️" else "🌙",
                            fontSize = 14.sp
                        )
                    }
                }

                // Overdue button
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { navController.navigate(Routes.OVERDUE_QUEUE) }
                ) {
                    Text(
                        text       = "Overdue",
                        fontSize   = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }

                // Logout button
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        SessionManager(context).clearSession()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text(
                        text       = "Logout",
                        fontSize   = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface,
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }

       // Hairline divider below header
        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )

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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape  = RoundedCornerShape(14.dp),
        color  = if (isOverdue)
            (if (ThemeManager.isDarkMode) Color(0xFF2A1A18) else Color(0xFFF0DCD2))
        else
            MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            0.5.dp,
            if (isOverdue) CareColors.Rose.copy(alpha = 0.22f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                if (request.priority.isNotEmpty()) {
                    PriorityBadge(priority = request.priority)
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Text(
                    text       = deadlineLabel,
                    fontSize   = 12.sp,
                    color      = if (isOverdue) CareColors.Rose else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text       = request.title,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.1).sp,
                maxLines   = 1,
                overflow   = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text     = request.address,
                fontSize = 13.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                StatusBadge(status = request.status)
                Text(
                    text       = request.category.replace("_", " "),
                    fontSize   = 11.sp,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val isDark = ThemeManager.isDarkMode
    val (fg, bg, border) = when (priority) {
        "HIGH"   -> if (isDark) Triple(CareColors.RoseDark, CareColors.RoseDark.copy(alpha = 0.14f), CareColors.RoseDark.copy(alpha = 0.28f))
        else Triple(CareColors.Rose, CareColors.RoseTint, CareColors.Rose.copy(alpha = 0.22f))
        "MEDIUM" -> if (isDark) Triple(CareColors.AmberDark, CareColors.AmberDark.copy(alpha = 0.14f), CareColors.AmberDark.copy(alpha = 0.28f))
        else Triple(CareColors.Amber, CareColors.AmberTint, CareColors.Amber.copy(alpha = 0.22f))
        else     -> if (isDark) Triple(CareColors.EmeraldDark, CareColors.EmeraldDark.copy(alpha = 0.14f), CareColors.EmeraldDark.copy(alpha = 0.28f))
        else Triple(CareColors.Emerald, CareColors.EmeraldTint, CareColors.Emerald.copy(alpha = 0.24f))
    }
    Surface(
        color  = bg,
        shape  = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, border)
    ) {
        Text(
            text     = priority,
            color    = fg,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val isDark = ThemeManager.isDarkMode
    val (fg, bg, border) = when (status) {
        "SUBMITTED"       -> if (isDark) Triple(CareColors.SlateDark, CareColors.SlateDark.copy(alpha = 0.12f), CareColors.SlateDark.copy(alpha = 0.24f))
        else Triple(CareColors.Slate, CareColors.SlateTint, CareColors.Slate.copy(alpha = 0.22f))
        "UNDER_REVIEW"    -> if (isDark) Triple(CareColors.AmberDark, CareColors.AmberDark.copy(alpha = 0.14f), CareColors.AmberDark.copy(alpha = 0.28f))
        else Triple(CareColors.Amber, CareColors.AmberTint, CareColors.Amber.copy(alpha = 0.22f))
        "ASSIGNED"        -> if (isDark) Triple(CareColors.IrisDark, CareColors.IrisDark.copy(alpha = 0.14f), CareColors.IrisDark.copy(alpha = 0.28f))
        else Triple(CareColors.Iris, CareColors.IrisTint, CareColors.Iris.copy(alpha = 0.22f))
        "VISIT_COMPLETED" -> if (isDark) Triple(CareColors.EmeraldDark, CareColors.EmeraldDark.copy(alpha = 0.14f), CareColors.EmeraldDark.copy(alpha = 0.28f))
        else Triple(CareColors.Emerald, CareColors.EmeraldTint, CareColors.Emerald.copy(alpha = 0.24f))
        "VERIFIED"        -> if (isDark) Triple(CareColors.EmeraldDark, CareColors.EmeraldDark.copy(alpha = 0.14f), CareColors.EmeraldDark.copy(alpha = 0.28f))
        else Triple(CareColors.Emerald, CareColors.EmeraldTint, CareColors.Emerald.copy(alpha = 0.24f))
        "ESCALATED"       -> if (isDark) Triple(CareColors.RoseDark, CareColors.RoseDark.copy(alpha = 0.14f), CareColors.RoseDark.copy(alpha = 0.28f))
        else Triple(CareColors.Rose, CareColors.RoseTint, CareColors.Rose.copy(alpha = 0.22f))
        else              -> if (isDark) Triple(CareColors.SlateDark, CareColors.SlateDark.copy(alpha = 0.12f), CareColors.SlateDark.copy(alpha = 0.24f))
        else Triple(CareColors.Slate, CareColors.SlateTint, CareColors.Slate.copy(alpha = 0.22f))
    }
    Surface(
        color  = bg,
        shape  = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, border)
    ) {
        Text(
            text          = status.replace("_", " "),
            color         = fg,
            fontSize      = 10.5.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            modifier      = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}