package com.deep.lumoraai.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppCard
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.ui.theme.tokens.Spacing

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppToolbar(title = "Settings") },
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "settings",
                onSelected = onNavigate
            )
        }
    ) { padding ->
        var showLanguageDialog by remember { mutableStateOf(false) }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.containerMargin, vertical = Spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Account", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                
                SettingsActionRow(
                    title = "Manage Profile",
                    subtitle = "Update your personal information",
                    onClick = { /* TODO */ }
                )
                
                SettingsActionRow(
                    title = "Subscription & Billing",
                    subtitle = "Manage your Pro plan",
                    onClick = { onNavigate(com.deep.lumoraai.core.navigation.Screen.Subscription.route) }
                )
                
                SettingsActionRow(
                    title = "Privacy & Security",
                    subtitle = "Protect your account data",
                    onClick = { /* TODO */ }
                )
                
                SettingsActionRow(
                    title = "Help & Support",
                    subtitle = "Contact us for assistance",
                    onClick = { /* TODO */ }
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text("Preferences", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                
                SettingsActionRow(
                    title = "Language",
                    subtitle = uiState.selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
                
                SettingsToggleRow(
                    title = "Dark Mode",
                    subtitle = "Use dark theme across the app",
                    checked = uiState.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
                
                SettingsToggleRow(
                    title = "Push Notifications",
                    subtitle = "Receive updates on generated tasks",
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) }
                )
                
                SettingsToggleRow(
                    title = "High Quality Mode",
                    subtitle = "Generate images in higher resolution (uses more credits)",
                    checked = uiState.highQualityMode,
                    onCheckedChange = { viewModel.toggleHighQualityMode(it) }
                )
            }

            if (uiState.isDevModeUnlocked) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text("Developer", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
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
                    .padding(vertical = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Version 1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.versionTapCount in 1..6) {
                    Text(
                        text = "${7 - uiState.versionTapCount} taps to unlock developer options",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = uiState.selectedLanguage,
                onLanguageSelected = { 
                    viewModel.setLanguage(it)
                    showLanguageDialog = false 
                },
                onDismissRequest = { showLanguageDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(">", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val languages = listOf("English", "Spanish", "French", "German", "Chinese", "Japanese", "Korean", "Hindi")
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(text = "Select Language", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            LazyColumn {
                items(languages) { language ->
                    val isSelected = language == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = language,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
                        )
                        if (isSelected) {
                            Text("✓", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}