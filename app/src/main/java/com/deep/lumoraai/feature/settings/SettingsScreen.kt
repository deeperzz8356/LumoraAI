package com.deep.lumoraai.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Theme colors matching Home/Profile pages
private val SettingsBackground = Color(0xFF081020)

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SettingsBackground)
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Account", 
            style = MaterialTheme.typography.titleLarge, 
            color = Color.White, 
            fontSize = 21.sp, 
            fontWeight = FontWeight.ExtraBold
        )
        
        Text(
            "Login / Signup - Access your account or create a new one",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        
        Text(
            "Settings content will be restored here...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}