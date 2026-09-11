package com.deep.lumoraai.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.R
import com.deep.lumoraai.core.nativeui.LumoraXmlScreen
import com.deep.lumoraai.core.nativeui.addActionRow
import com.deep.lumoraai.core.nativeui.addSectionTitle
import com.deep.lumoraai.core.nativeui.addTextCard
import com.deep.lumoraai.core.nativeui.resetContent
import com.deep.lumoraai.core.nativeui.setupTopBar
import com.deep.lumoraai.core.nativeui.toast

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LumoraXmlScreen(
        selectedTab = "settings",
        onNavigate = onNavigate,
        modifier = modifier
    ) { binding ->
        val context = binding.root.context
        binding.setupTopBar("Help & Support", "Support, guides and service status", onBack = onBack)
        binding.resetContent()

        binding.content.addSectionTitle("Get in touch")
        binding.content.addActionRow("Email Support", "support@lumora.ai", R.drawable.ic_lumora_info) {
            context.toast("Email support selected.")
        }
        binding.content.addActionRow("Live Chat", "Available 24/7", R.drawable.ic_lumora_bell) {
            context.toast("Live chat selected.")
        }

        binding.content.addSectionTitle("Resources")
        binding.content.addActionRow("FAQ Knowledge Base", "Answers to common Lumora questions", R.drawable.ic_lumora_info) {
            context.toast("FAQ selected.")
        }
        binding.content.addActionRow("Video Tutorials", "Learn creative workflows", R.drawable.ic_lumora_video) {
            context.toast("Tutorials selected.")
        }
        binding.content.addActionRow("Status Page", "Check service availability", R.drawable.ic_lumora_settings) {
            context.toast("Status page selected.")
        }

        binding.content.addSectionTitle("Report an Issue")
        binding.content.addActionRow("Report a Bug", "Send logs and details to the Lumora team", R.drawable.ic_lumora_info) {
            context.toast("Issue report selected.")
        }

        binding.content.addSectionTitle("Contact Info")
        binding.content.addTextCard("Email", "support@lumora.ai")
        binding.content.addTextCard("Phone", "+1 (555) 123-4567")
        binding.content.addTextCard("Hours", "24/7 support")
    }
}
