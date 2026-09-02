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
        if (email.isBlank() || password.isBlank()) {
            uiState = AuthUiState.Error("Fields cannot be empty.")
            return
        }
        uiState = AuthUiState.Loading
        val task = if (isSignUp) {
            auth.createUserWithEmailAndPassword(email, password)
        } else {
            auth.signInWithEmailAndPassword(email, password)
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
        uiState = AuthUiState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
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
