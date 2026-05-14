package com.carelink.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carelink.navigation.Routes
import com.carelink.navigation.getRoleHome
import com.carelink.viewmodel.AuthViewModel

// ─────────────────────────────────────────────────────────────────────────────
// LoginScreen — the first screen users see when not logged in.
// Uses AuthViewModel to check credentials against SQLite.
// On success, saves session to SharedPreferences and navigates to role dashboard.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()

    // Local state for form fields — uses mutableStateOf as taught in slides
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {

        // ── App title ────────────────────────────────────────────────────
        Text(
            text       = "CareLink",
            fontSize   = 32.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )

        Text(
            text     = "Northampton Council",
            fontSize = 14.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Email field ──────────────────────────────────────────────────
        OutlinedTextField(
            value         = email,
            onValueChange = { email = it; errorMsg = "" },
            label         = { Text("Email address") },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Password field ───────────────────────────────────────────────
        OutlinedTextField(
            value                = password,
            onValueChange        = { password = it; errorMsg = "" },
            label                = { Text("Password") },
            singleLine           = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier             = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Error message ────────────────────────────────────────────────
        if (errorMsg.isNotEmpty()) {
            Text(
                text  = errorMsg,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Sign in button ───────────────────────────────────────────────
        Button(
            onClick = {
                // Basic validation
                if (email.isBlank() || password.isBlank()) {
                    errorMsg = "Please enter your email and password"
                    return@Button
                }

                isLoading = true

                // Check credentials against SQLite database
                val user = viewModel.login(email.trim(), password.trim())

                if (user != null) {
                    // Save session to SharedPreferences
                    viewModel.saveSession(user)
                    // Navigate to the correct dashboard for this role
                    navController.navigate(getRoleHome(user.role)) {
                        // Clear the back stack so user can't go back to login
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                } else {
                    errorMsg  = "Invalid email or password"
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Signing in..." else "Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Register link ────────────────────────────────────────────────
        TextButton(onClick = { navController.navigate(Routes.REGISTER) }) {
            Text("New resident? Register here")
        }
    }
}