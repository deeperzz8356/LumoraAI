package com.deep.lumoraai.feature.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.utils.GuestIdentity
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
    val guestTrialExhausted = remember { mutableStateOf(GuestIdentity.isTrialExhausted(context)) }

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
        onGuestSignIn = {
            if (GuestIdentity.isTrialExhausted(context)) {
                guestTrialExhausted.value = true
                Toast.makeText(context, "Free trial finished. Please sign in or create an account.", Toast.LENGTH_LONG).show()
                viewModel.showEmailForm(false)
            } else {
                GuestIdentity.markTrialStarted(context)
                viewModel.signInAnonymously()
            }
        },
        onEmailOptionClick = { isSignUp -> viewModel.showEmailForm(isSignUp) },
        onBack = { viewModel.resetState() },
        allowGuestSignIn = !guestTrialExhausted.value
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
        Toast.makeText(context, "Google sign-in is not configured. Use email or guest access.", Toast.LENGTH_LONG).show()
        viewModel.resetState()
        return
    }
    scope.launch {
        try {
            val result = performGoogleSignIn(context, webClientId)
            viewModel.signInWithGoogle(result)
        } catch (e: GetCredentialException) {
            val message = when (e) {
                is NoCredentialException -> "No Google account found. Choose another sign-in method."
                else -> e.localizedMessage ?: "Google login failed."
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        } catch (e: Exception) {
            Toast.makeText(context, e.localizedMessage ?: "Google login failed.", Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }
}

private suspend fun performGoogleSignIn(context: Context, webClientId: String): String {
    val activity = context.findActivity()
        ?: throw IllegalStateException("Google sign-in requires an Activity context")
    val credentialManager = CredentialManager.create(context)

    return runCatching {
        requestGoogleIdToken(
            credentialManager = credentialManager,
            context = activity,
            webClientId = webClientId,
            filterAuthorized = true,
            autoSelect = true
        )
    }.recoverCatching {
        requestGoogleIdToken(
            credentialManager = credentialManager,
            context = activity,
            webClientId = webClientId,
            filterAuthorized = false,
            autoSelect = false
        )
    }.recoverCatching {
        requestSignInWithGoogle(credentialManager, activity, webClientId)
    }.getOrThrow()
}

private suspend fun requestGoogleIdToken(
    credentialManager: CredentialManager,
    context: Context,
    webClientId: String,
    filterAuthorized: Boolean,
    autoSelect: Boolean
): String {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(filterAuthorized)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(autoSelect)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    val result = credentialManager.getCredential(context = context, request = request)
    return extractGoogleIdToken(result.credential)
}

private suspend fun requestSignInWithGoogle(
    credentialManager: CredentialManager,
    context: Context,
    webClientId: String
): String {
    val googleOption = GetSignInWithGoogleOption.Builder(webClientId).build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleOption)
        .build()
    val result = credentialManager.getCredential(context = context, request = request)
    return extractGoogleIdToken(result.credential)
}

private fun extractGoogleIdToken(credential: Credential): String {
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
    throw IllegalArgumentException("Invalid credential type")
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
