package com.carelink.ui.coordinator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carelink.data.DatabaseHelper
import com.carelink.data.model.User
import com.carelink.data.model.UserRole
import com.carelink.util.DeadlineUtils
import com.carelink.viewmodel.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignScreen(
    navController: NavHostController,
    requestId: Int
) {
    val viewModel: RequestViewModel = viewModel()
    val context = LocalContext.current

    // Load the request
    LaunchedEffect(requestId) { viewModel.selectRequest(requestId) }

    val request = viewModel.selectedRequest

    // Load support workers from database
    val workers = remember {
        val db = DatabaseHelper(context).readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE role = ?",
            arrayOf(UserRole.WORKER)
        )
        val list = mutableListOf<User>()
        while (cursor.moveToNext()) {
            list.add(User(
                id       = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                email    = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                password = "",
                fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                role     = cursor.getString(cursor.getColumnIndexOrThrow("role"))
            ))
        }
        cursor.close()
        list
    }

    // Form state
    var selectedWorker   by remember { mutableStateOf<User?>(null) }
    var selectedPriority by remember { mutableStateOf("MEDIUM") }
    var coordNotes       by remember { mutableStateOf("") }
    var useTemplate      by remember { mutableStateOf(false) }
    var errorMsg         by remember { mutableStateOf("") }

    // Deadline template (additional feature A)
    val templateDeadline = request?.let {
        DeadlineUtils.getDeadlineFromTemplate(it.category)
    }
    var deadlineTs by remember { mutableStateOf<Long?>(null) }

    // Apply template when toggled
    LaunchedEffect(useTemplate, request) {
        deadlineTs = if (useTemplate) templateDeadline else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Request") },
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
                    Text(request.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(request.category.replace("_", " "), fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(request.address, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ── Priority selector ─────────────────────────────────────────
            Text("Priority *", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                    FilterChip(
                        selected = selectedPriority == p,
                        onClick  = { selectedPriority = p },
                        label    = { Text(p) }
                    )
                }
            }

            // ── Deadline template (additional feature A) ─
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Use Deadline Template", fontWeight = FontWeight.Medium)
                        Text(
                            text = when (request.category) {
                                "WELLBEING"      -> "4 hours (Wellbeing)"
                                "MISSED_CONTACT" -> "24 hours (Missed Contact)"
                                else             -> "72 hours (Routine)"
                            },
                            fontSize = 12.sp,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked         = useTemplate,
                        onCheckedChange = { useTemplate = it }
                    )
                }
            }

            // Show deadline label if set
            if (deadlineTs != null) {
                Text(
                    text  = "Deadline: ${DeadlineUtils.getDeadlineLabel(deadlineTs)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // ── Worker selector ───────────────────────────────────────────
            Text("Assign to Support Worker *", fontWeight = FontWeight.Medium)
            workers.forEach { worker ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedWorker?.id == worker.id,
                        onClick  = { selectedWorker = worker }
                    )
                    Text(worker.fullName, fontSize = 14.sp)
                }
            }

            // ── Coordinator notes ─────────────────────────────────────────
            OutlinedTextField(
                value         = coordNotes,
                onValueChange = { coordNotes = it },
                label         = { Text("Internal Notes (optional)") },
                minLines      = 2,
                maxLines      = 4,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Error message ─────────────────────────────────────────────
            if (errorMsg.isNotEmpty()) {
                Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
            }

            // ── Assign button ─────────────────────────────────────────────
            Button(
                onClick = {
                    when {
                        selectedWorker == null ->
                            errorMsg = "Please select a support worker"
                        deadlineTs == null ->
                            errorMsg = "Please use the deadline template or set a deadline"
                        else -> {
                            val success = viewModel.assignRequest(
                                requestId        = requestId,
                                workerId         = selectedWorker!!.id,
                                priority         = selectedPriority,
                                deadlineTs       = deadlineTs!!,
                                coordinatorNotes = coordNotes
                            )
                            if (success) navController.popBackStack()
                            else errorMsg = "Failed to assign request"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Assign to Worker")
            }
        }
    }
}