package com.deep.lumoraai.feature.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String, Boolean) -> Unit,
    onGuestSignIn: () -> Unit,
    onEmailOptionClick: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BrandLogo()
            HeroSection()
            Crossfade(
                targetState = uiState,
                label = "authForm",
                modifier = Modifier.weight(1f)
            ) { state ->
                when (state) {
                    AuthUiState.Loading -> AuthLoading()
                    is AuthUiState.EmailForm -> EmailAuthForm(
                        isSignUp = state.isSignUp,
                        onSubmit = onEmailSignIn,
                        onCancel = onBack
                    )
                    else -> AuthMainActions(
                        onGoogle = onGoogleSignIn,
                        onEmail = { onEmailOptionClick(false) },
                        onGuest = onGuestSignIn
                    )
                }
            }
            AuthFooter(
                uiState = uiState,
                onSignInClick = { onEmailOptionClick(false) },
                onSignUpClick = { onEmailOptionClick(true) }
            )
        }
    }
}

@Composable
private fun BrandLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .background(Color(0xFF131313), RoundedCornerShape(16.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                WandIcon(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "LUMORA",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 0.2.em,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Lumora AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Text(
                    text = "3",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HeroSection() {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color.White, Color(0xFFE0E0E0))
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Create Amazing Content with Next Generation AI",
            style = MaterialTheme.typography.headlineMedium.copy(
                brush = gradient
            ),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Unleash your creativity with powerful AI tools designed for everyone.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF8E8E8E),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AuthMainActions(
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
    onGuest: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "GET STARTED",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = Color(0xFF8E8E8E),
            letterSpacing = 0.2.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onGoogle,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoogleIcon(modifier = Modifier.size(20.dp))
                Text("Continue with Google", fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onEmail,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF131313),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF2A2A2A)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MailIcon(modifier = Modifier.size(20.dp))
                Text("Continue with Email", fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onGuest,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFF8E8E8E)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF2A2A2A)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserIcon(modifier = Modifier.size(20.dp))
                Text("Continue as Guest", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmailAuthForm(
    isSignUp: Boolean,
    onSubmit: (String, String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131313),
                unfocusedContainerColor = Color(0xFF131313),
                focusedIndicatorColor = Color(0xFFC2C5DF),
                unfocusedIndicatorColor = Color(0xFF2A2A2A),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color(0xFF8E8E8E)
            )
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131313),
                unfocusedContainerColor = Color(0xFF131313),
                focusedIndicatorColor = Color(0xFFC2C5DF),
                unfocusedIndicatorColor = Color(0xFF2A2A2A),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color(0xFF8E8E8E)
            )
        )
        Button(
            onClick = { onSubmit(email, password, isSignUp) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(if (isSignUp) "Sign Up" else "Sign In", fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Back to options",
            color = Color(0xFF8E8E8E),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onCancel() }
                .padding(8.dp)
        )
    }
}

@Composable
private fun AuthFooter(
    uiState: AuthUiState,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (uiState is AuthUiState.Error) {
            Text(
                text = uiState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Box(modifier = Modifier.weight(1f).height(0.5.dp).background(Color.White.copy(alpha = 0.1f)))
            Text(
                text = "or",
                color = Color(0xFF8E8E8E),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(modifier = Modifier.weight(1f).height(0.5.dp).background(Color.White.copy(alpha = 0.1f)))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isSignUp = uiState is AuthUiState.EmailForm && uiState.isSignUp
            Text(
                text = if (isSignUp) "Already have an account?" else "Don't have an account?",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8E8E8E)
            )
            Text(
                text = if (isSignUp) "Sign In" else "Sign Up",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.clickable {
                    if (isSignUp) onSignInClick() else onSignUpClick()
                }
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            ShieldIcon(modifier = Modifier.size(14.dp))
            Text(
                text = "Your data is secure",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8E8E8E)
            )
        }
        Text(
            text = "By continuing, you agree to Lumora AI 3's Terms of Service and Privacy Policy. \n© 2024 Lumora AI 3. All rights reserved.",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = Color(0xFF8E8E8E).copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuthLoading() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun WandIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.15f, size.height * 0.85f),
            end = Offset(size.width * 0.75f, size.height * 0.25f),
            strokeWidth = strokeWidth
        )
        drawCircle(
            color = Color.White,
            radius = size.width * 0.08f,
            center = Offset(size.width * 0.75f, size.height * 0.25f)
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.5f, size.height * 0.1f),
            end = Offset(size.width * 0.5f, size.height * 0.3f),
            strokeWidth = strokeWidth * 0.5f
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.4f, size.height * 0.2f),
            end = Offset(size.width * 0.6f, size.height * 0.2f),
            strokeWidth = strokeWidth * 0.5f
        )
    }
}

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            color = Color(0xFF4285F4),
            radius = size.width * 0.4f,
            center = Offset(size.width * 0.5f, size.height * 0.5f)
        )
    }
}

@Composable
private fun MailIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = Offset(w * 0.1f, h * 0.2f),
            size = Size(w * 0.8f, h * 0.6f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.1f, h * 0.2f),
            end = Offset(w * 0.5f, h * 0.5f),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(w * 0.9f, h * 0.2f),
            end = Offset(w * 0.5f, h * 0.5f),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
private fun UserIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = w * 0.25f,
            center = Offset(w * 0.5f, h * 0.35f)
        )
        drawArc(
            color = Color.White.copy(alpha = 0.6f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.65f),
            size = Size(w * 0.6f, h * 0.6f)
        )
    }
}

@Composable
private fun ShieldIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.8f, h * 0.25f)
            lineTo(w * 0.8f, h * 0.6f)
            quadraticTo(w * 0.8f, h * 0.85f, w * 0.5f, h * 0.95f)
            quadraticTo(w * 0.2f, h * 0.85f, w * 0.2f, h * 0.6f)
            lineTo(w * 0.2f, h * 0.25f)
            close()
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.6f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
