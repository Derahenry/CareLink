package com.carelink.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun RegisterScreen(navController: NavHostController) {
    val viewModel: AuthViewModel = viewModel()

    var fullName     by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var confirmPass  by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.RESIDENT) }
    var errorMsg     by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {

        // ── Header ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text          = "Create account",
                fontSize      = 18.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.2).sp
            )
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    text       = "Sign in",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )

        // ── Form ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Text(
                text      = "Register to request welfare checks for yourself or a neighbour. We'll only contact you about the requests you submit.",
                fontSize  = 13.5.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            // Full name
            CareTextField(
                label       = "Full name",
                value       = fullName,
                onValueChange = { fullName = it; errorMsg = "" },
                placeholder = "Sarah Mills"
            )

            // Email
            CareTextField(
                label       = "Email",
                value       = email,
                onValueChange = { email = it; errorMsg = "" },
                placeholder = "you@example.com"
            )

            // Password
            CareTextField(
                label       = "Password",
                value       = password,
                onValueChange = { password = it; errorMsg = "" },
                placeholder = "At least 6 characters",
                isPassword  = true
            )

            // Confirm password
            CareTextField(
                label       = "Confirm password",
                value       = confirmPass,
                onValueChange = { confirmPass = it; errorMsg = "" },
                placeholder = "Repeat password",
                isPassword  = true
            )

            // Role selector
            Text(
                text       = "I am registering as:",
                fontSize   = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.1.sp
            )

            Surface(
                shape  = RoundedCornerShape(12.dp),
                color  = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            ) {
                Column {
                    listOf(UserRole.RESIDENT to "Resident", UserRole.CARER to "Carer").forEachIndexed { index, (role, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedRole == role,
                                onClick  = { selectedRole = role },
                                colors   = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (index == 0) {
                            HorizontalDivider(
                                color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp,
                                modifier  = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            // Error
            if (errorMsg.isNotEmpty()) {
                Text(
                    text     = errorMsg,
                    color    = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Submit button
            Button(
                onClick = {
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
                    .height(50.dp),
                enabled = fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor      = MaterialTheme.colorScheme.onSurface,
                    contentColor        = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    disabledContentColor   = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                )
            ) {
                Text(
                    text       = "Create account",
                    fontSize   = 14.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


// CareTextField —

@Composable
fun CareTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false
) {
    Column {
        Text(
            text          = label,
            fontSize      = 12.5.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.1.sp,
            modifier      = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value                = value,
            onValueChange        = onValueChange,
            placeholder          = { Text(placeholder, fontSize = 14.sp) },
            singleLine           = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            shape                = RoundedCornerShape(12.dp),
            modifier             = Modifier.fillMaxWidth(),
            colors               = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}