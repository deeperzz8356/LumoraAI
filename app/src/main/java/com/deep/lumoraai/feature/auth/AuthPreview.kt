package com.deep.lumoraai.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.deep.lumoraai.core.theme.LumoraTheme

@Preview(name = "Auth Main", showBackground = true)
@Composable
fun AuthMainPreview() {
    LumoraTheme(darkTheme = true) {
        AuthScreen(
            uiState = AuthUiState.Initial,
            onGoogleSignIn = {},
            onEmailSignIn = { _, _, _ -> },
            onGuestSignIn = {},
            onEmailOptionClick = {},
            onBack = {}
        )
    }
}

@Preview(name = "Auth Email Sign In", showBackground = true)
@Composable
fun AuthEmailSignInPreview() {
    LumoraTheme(darkTheme = true) {
        AuthScreen(
            uiState = AuthUiState.EmailForm(isSignUp = false),
            onGoogleSignIn = {},
            onEmailSignIn = { _, _, _ -> },
            onGuestSignIn = {},
            onEmailOptionClick = {},
            onBack = {}
        )
    }
}

@Preview(name = "Auth Email Sign Up", showBackground = true)
@Composable
fun AuthEmailSignUpPreview() {
    LumoraTheme(darkTheme = true) {
        AuthScreen(
            uiState = AuthUiState.EmailForm(isSignUp = true),
            onGoogleSignIn = {},
            onEmailSignIn = { _, _, _ -> },
            onGuestSignIn = {},
            onEmailOptionClick = {},
            onBack = {}
        )
    }
}

@Preview(name = "Auth Tablet", device = Devices.TABLET, showBackground = true)
@Composable
fun AuthTabletPreview() {
    LumoraTheme(darkTheme = true) {
        AuthScreen(
            uiState = AuthUiState.Initial,
            onGoogleSignIn = {},
            onEmailSignIn = { _, _, _ -> },
            onGuestSignIn = {},
            onEmailOptionClick = {},
            onBack = {}
        )
    }
}
