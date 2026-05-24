package com.carelink.ui.reviewer

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
import com.carelink.navigation.Routes
import com.carelink.ui.coordinator.StatusBadge
import com.carelink.util.DeadlineUtils
import com.carelink.util.SessionManager
import com.carelink.viewmodel.RequestViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface

@Composable
fun ReviewQueueScreen(navController: NavHostController) {
    val viewModel: RequestViewModel = viewModel()
    val context = LocalContext.current
    val session = SessionManager(context)

    LaunchedEffect(Unit) { viewModel.loadCompletedForReview() }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Top bar ───────────────────────────────────────────────────────
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
                    text          = "Review Queue",
                    fontSize      = 18.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text     = "Welcome, ${session.getUserName()}",
                    fontSize = 12.5.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )

        // ── Queue list ────────────────────────────────────────────────────
        if (viewModel.requests.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "No visits awaiting review",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.requests) { request ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Routes.reviewDetail(request.id))
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
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
                                text     = request.address,
                                fontSize = 13.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text     = request.category.replace("_", " "),
                                fontSize = 12.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

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