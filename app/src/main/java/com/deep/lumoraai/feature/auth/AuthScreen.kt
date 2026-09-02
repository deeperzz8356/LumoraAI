package com.deep.lumoraai.feature.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.GoogleBrandIcon
import com.deep.lumoraai.core.components.LumoraIntroBackground
import com.deep.lumoraai.core.components.LumoraIntroLogo
import com.deep.lumoraai.core.components.LumoraIntroPrimaryButton
import com.deep.lumoraai.core.components.LumoraIntroSecondaryButton
import com.deep.lumoraai.core.components.LumoraIntroTextField
import com.deep.lumoraai.core.theme.IntroPalette

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String, Boolean) -> Unit,
    onGuestSignIn: () -> Unit,
    onEmailOptionClick: (Boolean) -> Unit,
    onBack: () -> Unit,
    allowGuestSignIn: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IntroPalette.BackgroundBase)
            .systemBarsPadding()
    ) {
        LumoraIntroBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            LumoraIntroLogo()
            Spacer(modifier = Modifier.height(24.dp))
            AuthHeroSection(uiState = uiState)
            Spacer(modifier = Modifier.height(32.dp))
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
                onSignUpClick = { onEmailOptionClick(true) },
                onGuestSignIn = onGuestSignIn,
                allowGuestSignIn = allowGuestSignIn
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AuthHeroSection(uiState: AuthUiState) {
    Crossfade(targetState = uiState, label = "authHero") { state ->
        when (state) {
            is AuthUiState.EmailForm -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (state.isSignUp) "Create your account" else "Welcome back",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntroPalette.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (state.isSignUp) {
                            "Sign up with email to save your creations and credits."
                        } else {
                            "Sign in with email to pick up where you left off."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = IntroPalette.TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Unlock",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntroPalette.TextPrimary
                    )
                    Text(
                        text = "AI Creation",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = IntroPalette.SecondaryText
                    )
                    Text(
                        text = "Images, video, and edits — all in one place.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IntroPalette.TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
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
        modifier = modifier.fillMaxWidth()
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
                onEmail = { onEmailOptionClick(false) }
            )
        }
    }
}

@Composable
private fun AuthMainActions(
    onGoogle: () -> Unit,
    onEmail: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "SIGN IN",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = IntroPalette.TextSubtle,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        LumoraIntroPrimaryButton(
            text = "Continue with Google",
            onClick = onGoogle,
            leadingContent = { GoogleBrandIcon(modifier = Modifier.size(20.dp)) }
        )
        LumoraIntroSecondaryButton(
            text = "Continue with Email",
            onClick = onEmail,
            leadingIcon = Icons.Default.Email,
            modifier = Modifier.fillMaxWidth()
        )
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
        modifier = Modifier.fillMaxWidth()
    ) {
        LumoraIntroTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email address"
        )
        LumoraIntroTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true
        )
        LumoraIntroPrimaryButton(
            text = if (isSignUp) "Create account" else "Sign in",
            onClick = { onSubmit(email, password, isSignUp) }
        )
        Text(
            text = "Back to sign-in options",
            color = IntroPalette.TextSubtle,
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
    onSignUpClick: () -> Unit,
    onGuestSignIn: () -> Unit,
    allowGuestSignIn: Boolean
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
        if (uiState !is AuthUiState.EmailForm && allowGuestSignIn) {
            TextButton(onClick = onGuestSignIn) {
                Text(
                    text = "Continue as guest",
                    style = MaterialTheme.typography.labelMedium,
                    color = IntroPalette.TextMuted
                )
            }
        } else if (uiState !is AuthUiState.EmailForm) {
            Text(
                text = "Free trial finished on this device. Sign in or create an account to continue.",
                color = IntroPalette.TextMuted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        FooterSwitchRow(
            uiState = uiState,
            onSignInClick = onSignInClick,
            onSignUpClick = onSignUpClick
        )
        FooterSecureRow()
        FooterCopyrightText()
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
            color = IntroPalette.TextSubtle
        )
        Text(
            text = if (isSignUp) "Sign in" else "Sign up",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = IntroPalette.TextPrimary,
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
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = IntroPalette.TextSubtle,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Your data is secure",
            style = MaterialTheme.typography.labelSmall,
            color = IntroPalette.TextSubtle
        )
    }
}

@Composable
private fun FooterCopyrightText() {
    Text(
        text = "By continuing, you agree to Lumora AI's Terms of Service and Privacy Policy.\n© 2026 Lumora AI. All rights reserved.",
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        color = IntroPalette.TextLegal,
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
        CircularProgressIndicator(
            color = IntroPalette.PrimaryButton,
            modifier = Modifier.size(36.dp)
        )
    }
}
