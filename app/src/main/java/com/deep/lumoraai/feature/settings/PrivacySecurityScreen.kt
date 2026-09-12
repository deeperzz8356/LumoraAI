package com.deep.lumoraai.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.deep.lumoraai.core.components.PremiumActionRow
import com.deep.lumoraai.core.components.PremiumHero
import com.deep.lumoraai.core.components.PremiumPage
import com.deep.lumoraai.core.components.PremiumSection
import com.deep.lumoraai.core.components.PremiumToggleRow
import com.deep.lumoraai.core.nativeui.toast

@Composable
fun PrivacySecurityScreen(
    onBack: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var dataCollection by remember { mutableStateOf(true) }
    var thirdPartySharing by remember { mutableStateOf(false) }
    var twoFactorAuth by remember { mutableStateOf(true) }
    var sessionTimeout by remember { mutableStateOf(true) }

    PremiumPage("Privacy & security", "Trust center", "Control account safety and data choices", onBack, modifier) {
        PremiumHero("Protected", "Your work stays yours", "Review what Lumora collects, how your account is protected, and what you can export.", Icons.Default.Security)
        PremiumSection("Privacy", "Choose how product data is used") {
            PremiumToggleRow("Usage diagnostics", "Share diagnostics that help improve Lumora", Icons.Default.Analytics, dataCollection, { dataCollection = it })
            PremiumToggleRow("Trusted providers", "Share limited analytics with essential providers", Icons.Default.Group, thirdPartySharing, { thirdPartySharing = it })
        }
        PremiumSection("Account protection", "Security controls for your sign-in") {
            PremiumToggleRow("Two-factor authentication", "Require another verification step", Icons.Default.VerifiedUser, twoFactorAuth, { twoFactorAuth = it })
            PremiumToggleRow("Session timeout", "Automatically close inactive sessions", Icons.Default.LockClock, sessionTimeout, { sessionTimeout = it })
        }
        PremiumSection("Your data") {
            PremiumActionRow("Download your data", "Request an export of your Lumora account", Icons.Default.Download, { context.toast("Data export request received.") })
            PremiumActionRow("Privacy policy", "Read how Lumora handles your information", Icons.Default.Policy, { context.toast("Privacy policy will open here.") })
        }
    }
}
