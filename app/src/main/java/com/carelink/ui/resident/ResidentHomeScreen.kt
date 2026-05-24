package com.carelink.ui.resident

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.carelink.navigation.Routes
import com.carelink.ui.coordinator.StatusBadge
import com.carelink.util.DeadlineUtils
import com.carelink.util.SessionManager
import com.carelink.viewmodel.AuthViewModel
import com.carelink.viewmodel.RequestViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import com.carelink.ui.theme.ThemeManager

@Composable
fun ResidentHomeScreen(navController: NavHostController) {
    val requestViewModel: RequestViewModel = viewModel()
    val authViewModel: AuthViewModel       = viewModel()
    val context = LocalContext.current
    val session = SessionManager(context)

    // Load this resident's requests on first render
    LaunchedEffect(Unit) { requestViewModel.loadMyRequests() }

    Scaffold(
        // Floating action button to create new request
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.NEW_REQUEST) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Request")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Top bar ───────────────────────────────────────────────────
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
                        text          = "My CareLink",
                        fontSize      = 18.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.2).sp
                    )
                    Text(
                        text     = session.getUserName(),
                        fontSize = 12.5.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { ThemeManager.isDarkMode = !ThemeManager.isDarkMode }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text     = if (ThemeManager.isDarkMode) "☀" else "🌙",
                                fontSize = 14.sp
                            )
                        }
                    }
                    Surface(
                        shape    = RoundedCornerShape(10.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant,
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
            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )

            // ── Request list ──────────────────────────────────────────────
            if (requestViewModel.requests.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text     = "No requests yet",
                            fontSize = 16.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text     = "Tap + to submit a welfare check request",
                            fontSize = 13.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding      = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(requestViewModel.requests) { request ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        Routes.requestDetail(request.id)
                                    )
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text       = request.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize   = 15.sp
                                    )
                                    StatusBadge(status = request.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text     = request.category.replace("_", " "),
                                    fontSize = 13.sp,
                                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text     = DeadlineUtils.getDeadlineLabel(request.deadlineTs),
                                    fontSize = 12.sp,
                                    color    = if (DeadlineUtils.isOverdue(request.deadlineTs))
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}