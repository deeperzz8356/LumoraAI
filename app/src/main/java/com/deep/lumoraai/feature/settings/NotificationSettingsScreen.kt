package com.deep.lumoraai.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Notification settings and preferences screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.notificationPreferences.collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notification Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        preferences?.let { prefs ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Master toggle
                SettingSection(
                    title = "Notifications",
                    description = "Enable or disable all notifications"
                ) {
                    SwitchSetting(
                        label = "All Notifications",
                        isChecked = prefs.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotificationsEnabled(it) }
                    )
                }

                // Notification types
                SettingSection(
                    title = "Notification Types",
                    description = "Choose which types of notifications to receive"
                ) {
                    SwitchSetting(
                        label = "Task Completion",
                        description = "When your AI tasks are finished",
                        isChecked = prefs.taskCompletionNotifications,
                        onCheckedChange = { viewModel.toggleTaskCompletionNotifications(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SwitchSetting(
                        label = "Engagement & Reminders",
                        description = "Usage reminders and suggestions",
                        isChecked = prefs.engagementNotifications,
                        onCheckedChange = { viewModel.toggleEngagementNotifications(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SwitchSetting(
                        label = "Feature Announcements",
                        description = "New features and updates",
                        isChecked = prefs.featureAnnouncementNotifications,
                        onCheckedChange = { viewModel.toggleFeatureAnnouncementNotifications(it) }
                    )
                }

                // Sound & Vibration
                SettingSection(
                    title = "Sound & Vibration",
                    description = "Configure notification alerts"
                ) {
                    SwitchSetting(
                        label = "Sound",
                        description = "Play notification sound",
                        isChecked = prefs.soundEnabled,
                        onCheckedChange = { viewModel.toggleSound(it) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SwitchSetting(
                        label = "Vibration",
                        description = "Vibrate for notifications",
                        isChecked = prefs.vibrationEnabled,
                        onCheckedChange = { viewModel.toggleVibration(it) }
                    )
                }

                // Do Not Disturb
                SettingSection(
                    title = "Do Not Disturb",
                    description = "Silence notifications during specific hours"
                ) {
                    SwitchSetting(
                        label = "Do Not Disturb",
                        description = "Enable quiet hours",
                        isChecked = prefs.doNotDisturbEnabled,
                        onCheckedChange = { viewModel.toggleDoNotDisturb(it) }
                    )

                    if (prefs.doNotDisturbEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "From ${String.format("%02d:00", prefs.doNotDisturbStartHour)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "to ${String.format("%02d:00", prefs.doNotDisturbEndHour)}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Frequency & Limits
                SettingSection(
                    title = "Frequency",
                    description = "Control notification frequency"
                ) {
                    Text(
                        text = "Frequency: ${prefs.notificationFrequency.uppercase()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "Max ${prefs.maxNotificationsPerDay} notifications per day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Action buttons
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = { viewModel.resetToDefaults() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Reset to Defaults")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Reusable settings section component
 */
@Composable
private fun SettingSection(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

/**
 * Switch setting component
 */
@Composable
private fun SwitchSetting(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
