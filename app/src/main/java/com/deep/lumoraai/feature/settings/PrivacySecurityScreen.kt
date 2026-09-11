package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addToggleRow
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.core.nativeui.toast

@Composable
fun PrivacySecurityScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dataCollection = remember { mutableStateOf(true) }
    val thirdPartySharing = remember { mutableStateOf(false) }
    val twoFactorAuth = remember { mutableStateOf(true) }
    val sessionTimeout = remember { mutableStateOf(true) }

    LumoraXmlScreen(
        selectedTab = "settings",
        onNavigate = onNavigate,
        modifier = modifier
    ) { binding ->
        val context = binding.root.context
        binding.setupTopBar("Privacy & Security", "Control account safety and data choices", onBack = onBack)
        binding.resetContent()

        binding.content.addSectionTitle("Privacy Settings")
        binding.content.addToggleRow(
            title = "Data Collection",
            subtitle = "Allow Lumora to collect diagnostics and usage analytics",
            checked = dataCollection.value,
        ) { dataCollection.value = it }
        binding.content.addToggleRow(
            title = "Third Party Sharing",
            subtitle = "Share limited analytics with trusted service providers",
            checked = thirdPartySharing.value,
        ) { thirdPartySharing.value = it }

        binding.content.addSectionTitle("Security Settings")
        binding.content.addToggleRow(
            title = "Two Factor Authentication",
            subtitle = "Add another layer of protection to your account",
            checked = twoFactorAuth.value,
        ) { twoFactorAuth.value = it }
        binding.content.addToggleRow(
            title = "Session Timeout",
            subtitle = "Automatically sign out inactive sessions",
            checked = sessionTimeout.value,
        ) { sessionTimeout.value = it }

        binding.content.addSectionTitle("Data Management")
        binding.content.addActionRow("Download Your Data", "Export your account data", R.drawable.ic_lumora_download) {
            context.toast("Data export request received.")
        }
        binding.content.addActionRow("View Privacy Policy", "Read how Lumora handles data", R.drawable.ic_lumora_info) {
            context.toast("Privacy policy will open here.")
        }
    }
}
