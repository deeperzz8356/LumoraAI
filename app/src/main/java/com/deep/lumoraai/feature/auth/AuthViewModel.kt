package com.deep.lumoraai.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deep.lumoraai.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
        val task = runCatching {
            if (isSignUp) {
                auth.createUserWithEmailAndPassword(cleanEmail, password)
            } else {
                auth.signInWithEmailAndPassword(cleanEmail, password)
            }
        }.getOrElse { error ->
            uiState = AuthUiState.Error(error.message ?: "Authentication could not start.")
            return
        }
        task.addOnCompleteListener { res ->
            if (res.isSuccessful) {
                finishWithBackendSync()
            } else {
                uiState = AuthUiState.Error(res.exception?.message ?: "Authentication failed.")
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
}
