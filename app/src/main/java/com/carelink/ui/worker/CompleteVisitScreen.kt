package com.carelink.ui.worker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carelink.ui.coordinator.PriorityBadge
import com.carelink.ui.coordinator.StatusBadge
import com.carelink.util.DeadlineUtils
import com.carelink.viewmodel.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteVisitScreen(
    navController: NavHostController,
    requestId: Int
) {
    val viewModel: RequestViewModel = viewModel()

    LaunchedEffect(requestId) { viewModel.selectRequest(requestId) }

    val request  = viewModel.selectedRequest
    var notes    by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Visit") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (request == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Request summary ───────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text       = request.title,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp
                        )
                        PriorityBadge(priority = request.priority)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text     = request.address,
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text     = request.category.replace("_", " "),
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Deadline indicator
                    val isOverdue = DeadlineUtils.isOverdue(request.deadlineTs)
                    Text(
                        text       = DeadlineUtils.getDeadlineLabel(request.deadlineTs),
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color      = if (isOverdue)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(status = request.status)
                }
            }

            // ── Description ───────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Description", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(request.description, fontSize = 13.sp)
                }
            }

            // ── Outcome notes (mandatory — additional feature C) ──────────
            Text(
                text       = "Visit Outcome Notes *",
                fontWeight = FontWeight.Medium,
                fontSize   = 14.sp
            )
            Text(
                text     = "Required before completing the visit",
                fontSize = 12.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it; errorMsg = "" },
                label         = { Text("Outcome notes *") },
                minLines      = 4,
                maxLines      = 8,
                modifier      = Modifier.fillMaxWidth(),
                isError       = errorMsg.isNotEmpty()
            )

            // ── Error message ─────────────────────────────────────────────
            if (errorMsg.isNotEmpty()) {
                Text(
                    text     = errorMsg,
                    color    = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            // ── Complete button ───────────────────────────────────────────
            Button(
                onClick = {
                    // Mandatory notes gate (additional feature C)
                    if (notes.isBlank()) {
                        errorMsg = "You must enter outcome notes before completing the visit"
                        return@Button
                    }
                    val success = viewModel.completeVisit(
                        requestId    = requestId,
                        outcomeNotes = notes
                    )
                    if (success) navController.popBackStack()
                    else errorMsg = viewModel.errorMessage
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Mark as Visit Completed")
            }
        }
    }
}