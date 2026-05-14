package com.carelink.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carelink.data.model.UserRole
import com.carelink.navigation.Routes
import com.carelink.viewmodel.AuthViewModel

// ─────────────────────────────────────────────────────────────────────────────
// RegisterScreen — allows Residents and Carers to self-register.
// Staff accounts are seeded in DatabaseHelper, not registered here.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()

    var fullName    by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.RESIDENT) }
    var errorMsg    by remember { mutableStateOf("") }
    var successMsg  by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Header ───────────────────────────────────────────────────────
        Text(
            text       = "Create Account",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Text(
            text     = "Residents and carers only",
            fontSize = 13.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Full name ────────────────────────────────────────────────────
        OutlinedTextField(
            value         = fullName,
            onValueChange = { fullName = it; errorMsg = "" },
            label         = { Text("Full name") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Email ────────────────────────────────────────────────────────
        OutlinedTextField(
            value         = email,
            onValueChange = { email = it; errorMsg = "" },
            label         = { Text("Email address") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Password ─────────────────────────────────────────────────────
        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it; errorMsg = "" },
            label                = { Text("Password") },
            singleLine           = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier             = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Confirm password ─────────────────────────────────────────────
        OutlinedTextField(
            value                = confirmPass,
            onValueChange        = { confirmPass = it; errorMsg = "" },
            label                = { Text("Confirm password") },
            singleLine           = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier             = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Role selector ────────────────────────────────────────────────
        Text(
            text     = "I am registering as:",
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedRole == UserRole.RESIDENT,
                onClick  = { selectedRole = UserRole.RESIDENT }
            )
            Text("Resident")
            Spacer(modifier = Modifier.width(24.dp))
            RadioButton(
                selected = selectedRole == UserRole.CARER,
                onClick  = { selectedRole = UserRole.CARER }
            )
            Text("Carer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Error / success messages ─────────────────────────────────────
        if (errorMsg.isNotEmpty()) {
            Text(text = errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (successMsg.isNotEmpty()) {
            Text(text = successMsg, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Register button ──────────────────────────────────────────────
        Button(
            onClick = {
                // Validate inputs
                when {
                    fullName.isBlank() || email.isBlank() || password.isBlank() ->
                        errorMsg = "Please fill in all fields"
                    password != confirmPass ->
                        errorMsg = "Passwords do not match"
                    password.length < 6 ->
                        errorMsg = "Password must be at least 6 characters"
                    else -> {
                        val success = viewModel.register(
                            email    = email.trim(),
                            password = password.trim(),
                            fullName = fullName.trim(),
                            role     = selectedRole
                        )
                        if (success) {
                            successMsg = "Account created! Please sign in."
                            // Navigate back to login after successful registration
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.REGISTER) { inclusive = true }
                            }
                        } else {
                            errorMsg = "Email already registered. Please sign in."
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Create Account")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Back to login ────────────────────────────────────────────────
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Already have an account? Sign in")
        }
    }
}