package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addPrimaryButton
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.addToggleRow
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.domain.model.NotificationFrequencies

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit = {},
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val prefs = uiState.preferences

    LumoraXmlScreen(modifier = modifier) { binding ->
        binding.setupTopBar("Notification Settings", "Choose what Lumora can send", onBack = onBack)
        binding.resetContent()
        if (uiState.isLoading) {
            binding.content.addTextCard("Loading", "Reading notification preferences.")
            return@LumoraXmlScreen
        }
        uiState.error?.let { binding.content.addTextCard("Error", it) }

        binding.content.addSectionTitle("Notifications")
        binding.content.addToggleRow("All Notifications", "Enable or mute all Lumora notifications", prefs.notificationsEnabled) {
            viewModel.toggleNotificationsEnabled(it)
        }

        binding.content.addSectionTitle("Notification Types")
        binding.content.addToggleRow("Task Completion", "When image or video jobs finish", prefs.taskCompletionNotifications) {
            viewModel.toggleTaskCompletionNotifications(it)
        }
        binding.content.addToggleRow("Engagement & Reminders", "Helpful reminders and activity updates", prefs.engagementNotifications) {
            viewModel.toggleEngagementNotifications(it)
        }
        binding.content.addToggleRow("Feature Announcements", "New tools and product updates", prefs.featureAnnouncementNotifications) {
            viewModel.toggleFeatureAnnouncementNotifications(it)
        }

        binding.content.addSectionTitle("Sound & Vibration")
        binding.content.addToggleRow("Sound", "Play a sound with alerts", prefs.soundEnabled) { viewModel.toggleSound(it) }
        binding.content.addToggleRow("Vibration", "Vibrate with alerts", prefs.vibrationEnabled) { viewModel.toggleVibration(it) }

        binding.content.addSectionTitle("Do Not Disturb")
        binding.content.addToggleRow("Do Not Disturb", "Pause notifications during quiet hours", prefs.doNotDisturbEnabled) {
            viewModel.toggleDoNotDisturb(it)
        }
        if (prefs.doNotDisturbEnabled) {
            binding.content.addTextCard("Quiet Hours", "From ${prefs.doNotDisturbStartHour}:00 to ${prefs.doNotDisturbEndHour}:00")
        }

        binding.content.addSectionTitle("Frequency & Limits")
        binding.content.addTextCard("Frequency", NotificationFrequencies.getDisplayName(prefs.notificationFrequency))
        binding.content.addTextCard("Max Notifications Per Day", prefs.maxNotificationsPerDay.toString())
        binding.content.addPrimaryButton("Reset to Defaults", enabled = !uiState.isSaving) {
            viewModel.resetToDefaults()
        }
    }
}
