package com.carelink.ui.resident

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
import com.carelink.data.model.Category
import com.carelink.viewmodel.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(navController: NavHostController) {
    val viewModel: RequestViewModel = viewModel()

    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address     by remember { mutableStateOf("") }
    var notes       by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf(Category.ROUTINE) }
    var errorMsg    by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Welfare Request") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save as draft button
                    TextButton(onClick = {
                        val saved = viewModel.submitRequest(
                            title       = title,
                            description = description,
                            category    = category,
                            address     = address,
                            notes       = notes,
                            asDraft     = true
                        )
                        if (saved) navController.popBackStack()
                        else errorMsg = viewModel.errorMessage
                    }) {
                        Text("Save Draft")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Title ─────────────────────────────────────────────────────
            OutlinedTextField(
                value         = title,
                onValueChange = { title = it; errorMsg = "" },
                label         = { Text("Title *") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Category selector ─────────────────────────────────────────
            Text("Concern Category *", fontWeight = FontWeight.Medium, fontSize = 14.sp)

            listOf(
                Category.ROUTINE        to "Routine Check",
                Category.MISSED_CONTACT to "Missed Contact",
                Category.WELLBEING      to "Wellbeing Concern"
            ).forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = category == value,
                        onClick  = { category = value }
                    )
                    Text(label, fontSize = 14.sp)
                }
            }

            // ── Address ───────────────────────────────────────────────────
            OutlinedTextField(
                value         = address,
                onValueChange = { address = it; errorMsg = "" },
                label         = { Text("Resident Address *") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Description ───────────────────────────────────────────────
            OutlinedTextField(
                value         = description,
                onValueChange = { description = it; errorMsg = "" },
                label         = { Text("Description *") },
                minLines      = 3,
                maxLines      = 5,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Optional notes ────────────────────────────────────────────
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                label         = { Text("Additional Notes (optional)") },
                minLines      = 2,
                maxLines      = 4,
                modifier      = Modifier.fillMaxWidth()
            )

            // ── Error message ─────────────────────────────────────────────
            if (errorMsg.isNotEmpty()) {
                Text(
                    text  = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Submit button ─────────────────────────────────────────────
            Button(
                onClick = {
                    isSubmitting = true
                    val success = viewModel.submitRequest(
                        title       = title,
                        description = description,
                        category    = category,
                        address     = address,
                        notes       = notes,
                        asDraft     = false
                    )
                    if (success) {
                        navController.popBackStack()
                    } else {
                        errorMsg     = viewModel.errorMessage
                        isSubmitting = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isSubmitting
            ) {
                Text(if (isSubmitting) "Submitting..." else "Submit Request")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}