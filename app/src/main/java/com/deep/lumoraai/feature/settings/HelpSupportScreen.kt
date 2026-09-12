package com.deep.lumoraai.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.deep.lumoraai.core.components.PremiumActionRow
import com.deep.lumoraai.core.components.PremiumHero
import com.deep.lumoraai.core.components.PremiumPage
import com.deep.lumoraai.core.components.PremiumSection
import com.deep.lumoraai.core.nativeui.toast

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    PremiumPage("Help & support", "Creator care", "Support, guides and service status", onBack, modifier) {
        PremiumHero("Always available", "Get unstuck quickly", "Find a direct answer, learn a workflow, or reach the Lumora support team.", Icons.Default.SupportAgent)
        PremiumSection("Talk to us", "Choose the support channel that suits you") {
            PremiumActionRow("Email support", "support@lumora.ai", Icons.Default.AlternateEmail, { context.toast("Email support selected.") }, trailing = "EMAIL")
            PremiumActionRow("Live chat", "Available around the clock", Icons.Default.ChatBubble, { context.toast("Live chat selected.") }, trailing = "24/7")
        }
        PremiumSection("Learn", "Answers and creative walkthroughs") {
            PremiumActionRow("Knowledge base", "Clear answers to common Lumora questions", Icons.AutoMirrored.Filled.MenuBook, { context.toast("FAQ selected.") })
            PremiumActionRow("Video tutorials", "Learn complete creative workflows", Icons.Default.PlayCircle, { context.toast("Tutorials selected.") })
            PremiumActionRow("Service status", "Check current system availability", Icons.Default.Verified, { context.toast("Status page selected.") }, trailing = "ONLINE")
        }
        PremiumSection("Something went wrong?") {
            PremiumActionRow("Report an issue", "Send details to the Lumora team", Icons.Default.BugReport, { context.toast("Issue report selected.") })
        }
    }
}
