package com.carelink.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.carelink.navigation.Routes
import com.carelink.navigation.getRoleHome
import com.carelink.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()

    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var errorMsg  by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 28.dp)
    ) {
        Column(
            modifier              = Modifier.fillMaxSize(),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {

            // ── Logo mark ─────────────────────────────────────────────
            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text       = "CL",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.surface,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Title ─────────────────────────────────────────────────
            Text(
                text          = "CareLink",
                fontSize      = 30.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.7).sp,
                textAlign     = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text      = "Northampton Council · Welfare coordination",
                fontSize  = 13.5.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Email ─────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text       = "Email",
                    fontSize   = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.1.sp,
                    modifier   = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it; errorMsg = "" },
                    placeholder   = { Text("you@northampton.gov.uk", fontSize = 14.sp) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor   = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Password ──────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text       = "Password",
                    fontSize   = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.1.sp,
                    modifier   = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value                = password,
                    onValueChange        = { password = it; errorMsg = "" },
                    placeholder          = { Text("••••••••", fontSize = 14.sp) },
                    singleLine           = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape                = RoundedCornerShape(12.dp),
                    modifier             = Modifier.fillMaxWidth(),
                    colors               = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor   = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // ── Error ─────────────────────────────────────────────────
            if (errorMsg.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text     = errorMsg,
                    color    = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Sign in button ────────────────────────────────────────
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMsg = "Please enter your email and password"
                        return@Button
                    }
                    isLoading = true
                    val user = viewModel.login(email.trim(), password.trim())
                    if (user != null) {
                        viewModel.saveSession(user)
                        navController.navigate(getRoleHome(user.role)) {
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
                enabled = !isLoading,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor   = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text       = if (isLoading) "Signing in…" else "Sign In",
                    fontSize   = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Register link ─────────────────────────────────────────
            TextButton(onClick = { navController.navigate(Routes.REGISTER) }) {
                Text(
                    text       = "New resident? Register here",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Footer ───────────────────────────────────────────────────
        Text(
            text          = "OFFICIAL · NORTHAMPTON COUNCIL · v1.0",
            fontSize      = 10.5.sp,
            fontWeight    = FontWeight.Medium,
            color         = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            letterSpacing = 0.6.sp,
            textAlign     = TextAlign.Center,
            modifier      = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        )
    }
}