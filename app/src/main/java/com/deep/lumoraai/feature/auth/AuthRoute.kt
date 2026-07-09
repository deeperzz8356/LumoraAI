package com.deep.lumoraai.feature.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AuthRoute(
    onNext: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onNext()
        }
    }

    AuthScreen(
        uiState = uiState,
        onGoogleSignIn = { triggerGoogleSignIn(context, scope, viewModel) },
        onEmailSignIn = { email, password, isSignUp ->
            viewModel.signInWithEmail(email, password, isSignUp)
        },
        onGuestSignIn = { viewModel.signInAnonymously() },
        onEmailOptionClick = { isSignUp -> viewModel.showEmailForm(isSignUp) },
        onBack = { viewModel.resetState() }
    )
}

private fun getWebClientId(context: Context): String? {
    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    val customResId = context.resources.getIdentifier("google_web_client_id", "string", context.packageName)
    val customClientId = if (customResId != 0) context.getString(customResId) else ""
    return if (customClientId.isNotBlank() && customClientId != "your_web_client_id_here") {
        customClientId
    } else if (resId != 0) {
        context.getString(resId)
    } else {
        null
    }
}

private fun triggerGoogleSignIn(
    context: Context,
    scope: CoroutineScope,
    viewModel: AuthViewModel
) {
    val webClientId = getWebClientId(context)
    if (webClientId == null) {
        Toast.makeText(context, "Google Client ID missing. Using Guest fallback.", Toast.LENGTH_LONG).show()
        viewModel.signInAnonymously()
        return
    }
    scope.launch {
        try {
            val result = performGoogleSignIn(context, webClientId)
            viewModel.signInWithGoogle(result)
        } catch (e: Exception) {
            Toast.makeText(context, "Google login failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

private suspend fun performGoogleSignIn(context: Context, webClientId: String): String {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    val result = credentialManager.getCredential(context = context, request = request)
    val credential = result.credential
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
    throw IllegalArgumentException("Invalid credential type")
}
