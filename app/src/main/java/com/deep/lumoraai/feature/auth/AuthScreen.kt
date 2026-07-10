package com.deep.lumoraai.feature.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F1026), Color(0xFF070714))
                )
            )
            .systemBarsPadding()
    ) {
        AuthContent(
            uiState = uiState,
            onGoogleSignIn = onGoogleSignIn,
            onEmailSignIn = onEmailSignIn,
            onGuestSignIn = onGuestSignIn,
            onEmailOptionClick = onEmailOptionClick,
            onBack = onBack
        )
    }
}

@Composable
fun AuthContent(
    uiState: AuthUiState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String, Boolean) -> Unit,
    onGuestSignIn: () -> Unit,
    onEmailOptionClick: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        BrandLogo()
        HeroSection()
        AuthFormContainer(
            uiState = uiState,
            onGoogleSignIn = onGoogleSignIn,
            onEmailSignIn = onEmailSignIn,
            onGuestSignIn = onGuestSignIn,
            onEmailOptionClick = onEmailOptionClick,
            onBack = onBack,
            modifier = Modifier.weight(1f)
        )
        AuthFooter(
            uiState = uiState,
            onSignInClick = { onEmailOptionClick(false) },
            onSignUpClick = { onEmailOptionClick(true) }
        )
    }
}

@Composable
fun AuthFormContainer(
    uiState: AuthUiState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String, Boolean) -> Unit,
    onGuestSignIn: () -> Unit,
    onEmailOptionClick: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = uiState,
        label = "authForm",
        modifier = modifier
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
}

@Composable
private fun BrandLogo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        LogoIconBox()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lumora AI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun LogoIconBox() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(80.dp)
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WandIcon(modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "LUMORA AI",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.8f),
                letterSpacing = 0.2.em,
                fontWeight = FontWeight.Bold
            )
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
            style = MaterialTheme.typography.headlineMedium.copy(brush = gradient),
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
        GoogleSignInButton(onClick = onGoogle)
        EmailSignInButton(onClick = onEmail)
        GuestSignInButton(onClick = onGuest)
    }
}

@Composable
private fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        GoogleIcon(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text("Continue with Google", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmailSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF161838),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        MailIcon(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text("Continue with Email", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GuestSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFF8E8E8E)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        UserIcon(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text("Continue as Guest", fontWeight = FontWeight.Bold)
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
        EmailTextField(email = email, onValueChange = { email = it })
        PasswordTextField(password = password, onValueChange = { password = it })
        Button(
            onClick = { onSubmit(email, password, isSignUp) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7E50EF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(if (isSignUp) "Sign Up" else "Sign In", fontWeight = FontWeight.Bold)
        }
        EmailCancelText(onCancel = onCancel)
    }
}

@Composable
private fun EmailTextField(email: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = email,
        onValueChange = onValueChange,
        label = { Text("Email Address") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E214A).copy(alpha = 0.6f),
            unfocusedContainerColor = Color(0xFF1E214A).copy(alpha = 0.6f),
            focusedBorderColor = Color(0xFFA855F7).copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color(0xFF8E8E8E)
        )
    )
}

@Composable
private fun PasswordTextField(password: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = password,
        onValueChange = onValueChange,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        visualTransformation = PasswordVisualTransformation(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E214A).copy(alpha = 0.6f),
            unfocusedContainerColor = Color(0xFF1E214A).copy(alpha = 0.6f),
            focusedBorderColor = Color(0xFFA855F7).copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color(0xFF8E8E8E)
        )
    )
}

@Composable
private fun EmailCancelText(onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = "Back to options",
        color = Color(0xFF8E8E8E),
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
            .clickable { onCancel() }
            .padding(8.dp)
    )
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
        FooterOrDivider()
        FooterSwitchRow(uiState = uiState, onSignInClick = onSignInClick, onSignUpClick = onSignUpClick)
        FooterSecureRow()
        FooterCopyrightText()
    }
}

@Composable
private fun FooterOrDivider() {
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
}

@Composable
private fun FooterSwitchRow(
    uiState: AuthUiState,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
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
}

@Composable
private fun FooterSecureRow() {
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
}

@Composable
private fun FooterCopyrightText() {
    Text(
        text = "By continuing, you agree to Lumora AI's Terms of Service and Privacy Policy. \n© 2024 Lumora AI. All rights reserved.",
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        color = Color(0xFF8E8E8E).copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
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
        val scaleX = size.width / 24f
        val scaleY = size.height / 24f
        drawContext.canvas.save()
        drawContext.transform.scale(scaleX, scaleY)
        drawGoogleSegments()
        drawContext.canvas.restore()
    }
}

private fun DrawScope.drawGoogleSegments() {
    val parser = PathParser()
    
    val bluePath = parser.parsePathString("M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z").toPath()
    drawPath(bluePath, Color(0xFF4285F4))
    
    val greenPath = PathParser().parsePathString("M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z").toPath()
    drawPath(greenPath, Color(0xFF34A853))
    
    val yellowPath = PathParser().parsePathString("M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z").toPath()
    drawPath(yellowPath, Color(0xFFFBBC05))
    
    val redPath = PathParser().parsePathString("M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z").toPath()
    drawPath(redPath, Color(0xFFEA4335))
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
