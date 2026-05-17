package com.carelink.ui.reviewer

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
import com.carelink.viewmodel.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewDetailScreen(
    navController: NavHostController,
    requestId: Int
) {
    val viewModel: RequestViewModel = viewModel()

    LaunchedEffect(requestId) { viewModel.selectRequest(requestId) }

    val request = viewModel.selectedRequest

    // Escalation reasons (additional feature D — structured reasons)
    val escalationReasons = listOf(
        "Safeguarding concern identified",
        "Resident not found at address",
        "Medical attention required",
        "Further investigation needed",
        "Incorrect outcome recorded"
    )

    var selectedReason  by remember { mutableStateOf("") }
    var reviewNotes     by remember { mutableStateOf("") }
    var showEscalate    by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Visit") },
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
                        if (request.priority.isNotEmpty()) {
                            PriorityBadge(priority = request.priority)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(request.address, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(request.category.replace("_", " "), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
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

            // ── Coordinator notes ─────────────────────────────────────────
            if (request.coordinatorNotes.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Coordinator Notes", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(request.coordinatorNotes, fontSize = 13.sp)
                    }
                }
            }

            // ── Review notes ──────────────────────────────────────────────
            OutlinedTextField(
                value         = reviewNotes,
                onValueChange = { reviewNotes = it; errorMsg = "" },
                label         = { Text("Review notes (optional)") },
                minLines      = 3,
                maxLines      = 5,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Escalation reasons (additional feature D) ─────────────────
            if (showEscalate) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text       = "Select Escalation Reason *",
                            fontWeight = FontWeight.Medium,
                            fontSize   = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        escalationReasons.forEach { reason ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier          = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedReason == reason,
                                    onClick  = { selectedReason = reason }
                                )
                                Text(reason, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ── Error message ─────────────────────────────────────────────
            if (errorMsg.isNotEmpty()) {
                Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            // ── Action buttons ────────────────────────────────────────────
            if (!showEscalate) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Verify button
                    Button(
                        onClick = {
                            val success = viewModel.verifyOutcome(requestId, reviewNotes)
                            if (success) navController.popBackStack()
                            else errorMsg = "Failed to verify outcome"
                        },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("✓ Verify")
                    }

                    // Escalate button
                    OutlinedButton(
                        onClick = { showEscalate = true },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("⚠ Escalate")
                    }
                }
            } else {
                // Confirm escalation
                Button(
                    onClick = {
                        if (selectedReason.isEmpty()) {
                            errorMsg = "Please select an escalation reason"
                            return@Button
                        }
                        val success = viewModel.escalateCase(requestId, selectedReason, reviewNotes)
                        if (success) navController.popBackStack()
                        else errorMsg = "Failed to escalate case"
                    },
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Confirm Escalation")
                }

                TextButton(
                    onClick  = { showEscalate = false; selectedReason = "" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}