package com.deep.lumoraai.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModel : ViewModel() {
    var uiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set

    init { load() }

    fun load() {
        val user = FirebaseAuth.getInstance().currentUser
        val items = if (user != null) {
            val name = user.displayName ?: user.email?.substringBefore("@") ?: "Guest User"
            val email = user.email ?: "Anonymous Access"
            val plan = if (user.isAnonymous) "Free Tier (Guest)" else "Premium Account"
            listOf(name, email, plan)
        } else {
            listOf("Not Logged In", "Please register or sign in.")
        }
        uiState = ProfileUiState.Success(items)
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }
}