package com.deep.lumoraai.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.ui.theme.tokens.Spacing
import com.google.firebase.auth.FirebaseAuth

// Theme colors matching Home/Profile pages
private val SettingsBackground = Color(0xFF081020)
private val SettingsCard = Color(0xFF10192D)
private val SettingsStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SettingsBackground,
        topBar = { SettingsHeader() },
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "settings",
                onSelected = onNavigate
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsBackground)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Account", style = MaterialTheme.typography.titleLarge, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                
                SettingsActionRow(
                    title = "Manage Profile",
                    subtitle = "Update your personal information",
                    onClick = {
                        val user = FirebaseAuth.getInstance().currentUser
                        onNavigate(if (user == null || user.isAnonymous) Screen.Auth.route else Screen.EditProfile.route)
                    }
                )
                
                SettingsActionRow(
                    title = "Subscription & Billing",
                    subtitle = "Manage your Pro plan",
                    onClick = { onNavigate(Screen.Subscription.route) }
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Preferences", style = MaterialTheme.typography.titleLarge, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                
                SettingsActionRow(
                    title = "Language",
                    subtitle = uiState.selectedLanguage,
                    onClick = { onNavigate("${Screen.Language.route}?source=settings") }
                )
            }

            if (uiState.isDevModeUnlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Developer", style = MaterialTheme.typography.titleLarge, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    SettingsToggleRow(
                        title = "Developer Mode",
                        subtitle = if (uiState.isDeveloperMode) "Unlimited trial — no restrictions" else "User mode — normal restrictions apply",
                        checked = uiState.isDeveloperMode,
                        onCheckedChange = { viewModel.toggleDeveloperMode(it) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onVersionTapped() }
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Version 1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
                if (uiState.versionTapCount in 1..6) {
                    Text(
                        text = "${7 - uiState.versionTapCount} taps to unlock developer options",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(Color(0xFF0B1426))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Lime.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Lime, modifier = Modifier.size(21.dp))
        }
        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = SettingsCard,
        border = BorderStroke(1.dp, SettingsStroke.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Lime,
                    checkedTrackColor = Lime.copy(alpha = 0.3f),
                    uncheckedThumbColor = Muted,
                    uncheckedTrackColor = SettingsStroke
                )
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = SettingsCard,
        border = BorderStroke(1.dp, SettingsStroke.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Lime.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Lime,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
