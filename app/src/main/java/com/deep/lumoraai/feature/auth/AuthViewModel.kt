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
import com.google.firebase.auth.AuthCredential
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

    /**
     * Clears a stuck full-screen loading state. Only resets when currently
     * Loading so it never clobbers a Success (which drives navigation) or an
     * Error the user still needs to see. Safe to call on navigation-away.
     */
    fun clearLoading() {
        if (uiState is AuthUiState.Loading) {
            uiState = AuthUiState.Initial
        }
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
                // Track whether this flow actually created a new account so we can
                // give the user explicit confirmation.
                var createdNewAccount = false
                if (currentUser?.isAnonymous == true) {
                    runCatching {
                        currentUser.linkWithCredential(credential).await()
                        // Upgrading a guest into a real account is a new account.
                        createdNewAccount = true
                    }.getOrElse {
                        auth.signInWithEmailAndPassword(cleanEmail, password).await()
                        createdNewAccount = false
                    }
                } else if (isSignUp) {
                    auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                    createdNewAccount = true
                } else {
                    auth.signInWithEmailAndPassword(cleanEmail, password).await()
                }
                finishWithBackendSync(isNewAccount = createdNewAccount)
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
        val currentUser = auth.currentUser
        viewModelScope.launch {
            try {
                if (currentUser?.isAnonymous == true) {
                    signInOrLinkGuest(credential)
                } else {
                    auth.signInWithCredential(credential).await()
                }
                finishWithBackendSync()
            } catch (error: Exception) {
                uiState = AuthUiState.Error(error.localizedMessage ?: "Google login failed.")
            }
        }
    }

    private suspend fun signInOrLinkGuest(credential: AuthCredential) {
        val guest = auth.currentUser
        if (guest?.isAnonymous == true) {
            runCatching {
                guest.linkWithCredential(credential).await()
            }.getOrElse {
                auth.signInWithCredential(credential).await()
            }
        } else {
            auth.signInWithCredential(credential).await()
        }
    }

    private fun finishWithBackendSync(isNewAccount: Boolean = false) {
        uiState = AuthUiState.Success(isNewAccount = isNewAccount)
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
