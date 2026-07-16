package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudyViewModel

enum class AuthMode {
    LOGIN, SIGNUP, FORGOT_PASSWORD, VERIFICATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Student") } // "Student" or "Admin"
    
    val verificationSent by viewModel.verificationSent.collectAsState()
    val isEmailVerified by viewModel.isEmailVerified.collectAsState()
    val passwordResetSent by viewModel.passwordResetSent.collectAsState()
    
    var feedbackMessage by remember { mutableStateOf("") }
    var isErrorFeedback by remember { mutableStateOf(false) }

    val isEmailValid = email.contains("@") && email.length > 5
    val isPasswordValid = password.length >= 6

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // --- APP LOGO ICON ---
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(BentoPremiumBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "StudyGenie Logo",
                tint = BentoPremiumText,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "StudyGenie AI",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BentoTextPrimary
        )

        Text(
            text = "Intelligent Bento Study Assistant",
            fontSize = 13.sp,
            color = BentoTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // --- MAIN INPUT FORM CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
            border = BorderStroke(1.dp, BentoCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Title inside form
                Text(
                    text = when (mode) {
                        AuthMode.LOGIN -> "Welcome Back"
                        AuthMode.SIGNUP -> "Create Account"
                        AuthMode.FORGOT_PASSWORD -> "Forgot Password"
                        AuthMode.VERIFICATION -> "Email Verification"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )

                // Feedback indicator
                if (feedbackMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isErrorFeedback) Color(0xFFF8D7DA) else BentoPremiumBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = feedbackMessage,
                            color = if (isErrorFeedback) Color(0xFF721C24) else BentoPremiumText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                when (mode) {
                    AuthMode.LOGIN -> {
                        // Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; feedbackMessage = "" },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPremiumBtn,
                                unfocusedBorderColor = BentoCardBorder
                            )
                        )

                        // Password Input
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; feedbackMessage = "" },
                            label = { Text("Password (min 6 chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, "Lock") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPremiumBtn,
                                unfocusedBorderColor = BentoCardBorder
                            )
                        )

                        // Role Selector Header
                        Text(
                            text = "Select Account Role:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Role toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Student", "Admin").forEach { role ->
                                val isSelected = selectedRole == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) BentoPremiumBg else BentoBackground)
                                        .border(
                                            BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) BentoPremiumBtn else BentoCardBorder
                                            ),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { selectedRole = role }
                                        .padding(vertical = 12.dp)
                                        .testTag("role_btn_$role"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = role,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) BentoPremiumText else BentoTextPrimary
                                    )
                                }
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = {
                                if (isEmailValid && isPasswordValid) {
                                    val success = viewModel.login(email, selectedRole)
                                    if (success) {
                                        feedbackMessage = ""
                                    } else {
                                        isErrorFeedback = true
                                        feedbackMessage = "Invalid credentials format."
                                    }
                                } else {
                                    isErrorFeedback = true
                                    feedbackMessage = "Please enter a valid email and 6+ character password."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_submit_login_btn")
                        ) {
                            Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    AuthMode.SIGNUP -> {
                        // Full Name Input
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; feedbackMessage = "" },
                            label = { Text("Student Name") },
                            leadingIcon = { Icon(Icons.Default.Person, "Name") },
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPremiumBtn,
                                unfocusedBorderColor = BentoCardBorder
                            )
                        )

                        // Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; feedbackMessage = "" },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPremiumBtn,
                                unfocusedBorderColor = BentoCardBorder
                            )
                        )

                        // Password Input
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; feedbackMessage = "" },
                            label = { Text("Password (min 6 chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, "Lock") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPremiumBtn,
                                unfocusedBorderColor = BentoCardBorder
                            )
                        )

                        // Submit Button
                        Button(
                            onClick = {
                                if (name.trim().isNotEmpty() && isEmailValid && isPasswordValid) {
                                    viewModel.signUp(email, name, "Student")
                                    mode = AuthMode.VERIFICATION
                                    feedbackMessage = "Verification email successfully sent to $email."
                                    isErrorFeedback = false
                                } else {
                                    isErrorFeedback = true
                                    feedbackMessage = "Fill in all fields correctly."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_submit_signup_btn")
                        ) {
                            Text("Create Account", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    AuthMode.FORGOT_PASSWORD -> {
                        Text(
                            text = "Enter your verified email to receive an auto-generated password reset instruction.",
                            fontSize = 12.sp,
                            color = BentoTextSecondary,
                            lineHeight = 16.sp
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; feedbackMessage = "" },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            modifier = Modifier.fillMaxWidth().testTag("auth_forgot_email_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPremiumBtn,
                                unfocusedBorderColor = BentoCardBorder
                            )
                        )

                        Button(
                            onClick = {
                                if (isEmailValid) {
                                    viewModel.sendForgotPasswordEmail(email)
                                    isErrorFeedback = false
                                    feedbackMessage = "Instructions sent! Please check your spam folder."
                                } else {
                                    isErrorFeedback = true
                                    feedbackMessage = "Enter a valid email address."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_submit_forgot_btn")
                        ) {
                            Text("Send Reset Link", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    AuthMode.VERIFICATION -> {
                        Text(
                            text = "Please verify your account to unlock AI Study features. We've sent an offline mock verification link.",
                            fontSize = 12.sp,
                            color = BentoTextSecondary,
                            lineHeight = 16.sp
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.dp, BentoCardBorder), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Verify Shield",
                                    tint = BentoPremiumBtn,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Pending Verification",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextPrimary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.verifyEmail()
                                isErrorFeedback = false
                                feedbackMessage = "Email verified successfully!"
                                mode = AuthMode.LOGIN
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPremiumBtn),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_confirm_verify_btn")
                        ) {
                            Text("Verify Account (Offline Demo)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- SUB FOOTER SWITCH LINKS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (mode) {
                    AuthMode.LOGIN -> "Don't have an account? "
                    AuthMode.SIGNUP -> "Already have an account? "
                    AuthMode.FORGOT_PASSWORD -> "Back to "
                    AuthMode.VERIFICATION -> "Want to try another "
                },
                fontSize = 13.sp,
                color = BentoTextSecondary
            )
            Text(
                text = when (mode) {
                    AuthMode.LOGIN -> "Sign Up"
                    AuthMode.SIGNUP -> "Sign In"
                    AuthMode.FORGOT_PASSWORD -> "Sign In"
                    AuthMode.VERIFICATION -> "Login Mode"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BentoPremiumBtn,
                modifier = Modifier
                    .clickable {
                        feedbackMessage = ""
                        mode = when (mode) {
                            AuthMode.LOGIN -> AuthMode.SIGNUP
                            AuthMode.SIGNUP -> AuthMode.LOGIN
                            AuthMode.FORGOT_PASSWORD -> AuthMode.LOGIN
                            AuthMode.VERIFICATION -> AuthMode.LOGIN
                        }
                    }
                    .testTag("auth_switch_mode_btn")
            )
        }

        if (mode == AuthMode.LOGIN) {
            Text(
                text = "Forgot Password?",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextSecondary,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable {
                        feedbackMessage = ""
                        mode = AuthMode.FORGOT_PASSWORD
                    }
                    .testTag("auth_forgot_password_link")
            )
        }
    }
}
