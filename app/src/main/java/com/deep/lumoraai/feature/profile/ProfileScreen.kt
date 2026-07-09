package com.deep.lumoraai.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.EmptyState
import com.deep.lumoraai.core.components.ErrorState
import com.deep.lumoraai.core.components.GradientButton
import com.deep.lumoraai.core.components.Loading
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import com.deep.lumoraai.feature.profile.components.ProfileFeatureCard

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNext: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "Profile") },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "profile",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        when (uiState) {
            ProfileUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            ProfileUiState.Empty -> EmptyState(title = "Profile", message = "No data yet.", modifier = Modifier.padding(padding))
            is ProfileUiState.Error -> ErrorState(title = "Profile", message = uiState.message, modifier = Modifier.padding(padding))
            is ProfileUiState.Success -> ProfileContent(
                items = uiState.items,
                onNext = onNext,
                onSignOut = onSignOut,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ProfileContent(
    items: List<String>,
    onNext: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Manage your authenticated session and settings.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        items.forEach { item -> ProfileFeatureCard(title = item, subtitle = "Account Information") }
        GradientButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2C1616),
                contentColor = Color(0xFFFF7A7A)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFFF7A7A).copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Sign Out", fontWeight = FontWeight.Bold)
        }
    }
}