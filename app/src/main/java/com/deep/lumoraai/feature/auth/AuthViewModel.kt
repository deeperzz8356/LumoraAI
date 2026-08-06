package com.deep.lumoraai.feature.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.logInWith

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
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
                linkRevenueCatUser(auth.currentUser?.uid)
                uiState = AuthUiState.Success
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
                linkRevenueCatUser(auth.currentUser?.uid)
                uiState = AuthUiState.Success
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
                linkRevenueCatUser(auth.currentUser?.uid)
                uiState = AuthUiState.Success
            } else {
                uiState = AuthUiState.Error(task.exception?.message ?: "Google login failed.")
            }
        }
    }

    private fun linkRevenueCatUser(uid: String?) {
        if (uid.isNullOrBlank()) return
        Purchases.sharedInstance.logInWith(
            appUserID = uid,
            onError = { error ->
                Log.e(TAG, "RC login failed: ${error.message}")
            },
            onSuccess = { customerInfo, created ->
                Log.d(TAG, "RC login ok created=$created entitlements=${customerInfo.entitlements.active.keys}")
            }
        )
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}
