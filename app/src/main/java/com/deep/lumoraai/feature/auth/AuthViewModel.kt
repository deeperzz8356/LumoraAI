package com.deep.lumoraai.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    var uiState: AuthUiState by mutableStateOf(AuthUiState.Initial)
        private set

    fun resetState() {
        uiState = AuthUiState.Initial
    }

    fun showEmailForm(isSignUp: Boolean) {
        uiState = AuthUiState.EmailForm(isSignUp)
    }

    fun signInAnonymously() {
        uiState = AuthUiState.Loading
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finishWithBackendSync()
            } else {
                uiState = AuthUiState.Error(task.exception?.message ?: "Guest access failed.")
            }
        }
    }

    fun signInWithEmail(email: String, password: String, isSignUp: Boolean) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            uiState = AuthUiState.Error("Enter email and password.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            uiState = AuthUiState.Error("Enter a valid email address.")
            return
        }
        if (password.length < 6) {
            uiState = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }
        uiState = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                val credential = EmailAuthProvider.getCredential(cleanEmail, password)
                if (isSignUp && currentUser?.isAnonymous == true) {
                    currentUser.linkWithCredential(credential).await()
                } else if (isSignUp) {
                    auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                } else {
                    auth.signInWithEmailAndPassword(cleanEmail, password).await()
                }
                finishWithBackendSync()
            } catch (error: Exception) {
                uiState = AuthUiState.Error(authenticationMessage(error))
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            uiState = AuthUiState.Error("Google login returned an empty credential.")
            return
        }
        uiState = AuthUiState.Loading
        val credential = runCatching {
            GoogleAuthProvider.getCredential(idToken, null)
        }.getOrElse { error ->
            uiState = AuthUiState.Error(error.localizedMessage ?: "Google login failed.")
            return
        }
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finishWithBackendSync()
            } else {
                uiState = AuthUiState.Error(task.exception?.message ?: "Google login failed.")
            }
        }
    }

    private fun finishWithBackendSync() {
        uiState = AuthUiState.Success
        viewModelScope.launch {
            authRepository.syncCurrentUser()
        }
    }

    private fun authenticationMessage(error: Exception): String =
        when ((error as? FirebaseAuthException)?.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered. Sign in instead."
            "ERROR_INVALID_EMAIL" -> "Enter a valid email address."
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Email or password is incorrect."
            "ERROR_USER_NOT_FOUND" -> "No account was found for this email."
            "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
            else -> error.localizedMessage ?: "Authentication failed. Please try again."
        }
}
