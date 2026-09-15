package com.deep.lumoraai.feature.settings

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.R
import com.deep.lumoraai.BuildConfig
import com.deep.lumoraai.ads.LocalAdsConfigStore
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.databinding.ScreenSettingsRootBinding
import com.google.firebase.auth.FirebaseAuth
import compose.icons.TablerIcons
import compose.icons.tablericons.Language

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleHighQualityMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val user = FirebaseAuth.getInstance().currentUser
    // Read subscription toggle from Remote Config — hide the billing row when off.
    val config = LocalAdsConfigStore.current?.current
    val subscriptionEnabled = config?.subscriptionEnabled ?: true
    val privacyPolicyUrl = config?.privacyPolicyUrl ?: "https://lumoraai.example/privacy-policy"
    val termsAndConditionsUrl = config?.termsAndConditionsUrl ?: "https://lumoraai.example/terms-and-conditions"
    val appShareUrl = config?.appShareUrl ?: "https://play.google.com/store/apps/details?id=com.deep.lumoraai"

    AndroidView(
        factory = { context ->
            val binding = ScreenSettingsRootBinding.inflate(LayoutInflater.from(context))
            binding.root.tag = binding
            binding.root
        },
        update = { root ->
            val binding = root.tag as ScreenSettingsRootBinding

            binding.versionText.text = "Version ${BuildConfig.VERSION_NAME}"
            binding.languageSubtitle.text = uiState.selectedLanguage.ifBlank { "English" }

            // Language icon — TablerIcons.Language via ComposeView
            binding.languageIconHost.setContent {
                Icon(
                    imageVector = TablerIcons.Language,
                    contentDescription = null,
                    tint = Color(0xFFD6FF3F),
                    modifier = Modifier.size(24.dp),
                )
            }

            // Hide Subscription & Billing row when subscription is disabled remotely
            binding.subscriptionRow.visibility =
                if (subscriptionEnabled) View.VISIBLE else View.GONE

            binding.backButton.setOnClickListener { onBack() }
            binding.manageProfileRow.setOnClickListener {
                onNavigate(
                    if (user == null || user.isAnonymous) Screen.Auth.route
                    else Screen.EditProfile.route
                )
            }
            binding.subscriptionRow.setOnClickListener {
                if (subscriptionEnabled) {
                    onNavigate(Screen.Subscription.route)
                }
            }
            binding.languageRow.setOnClickListener {
                onNavigate("${Screen.Language.route}?source=settings")
            }
            binding.privacyPolicyRow.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                root.context.startActivity(intent)
            }
            binding.termsRow.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(termsAndConditionsUrl))
                root.context.startActivity(intent)
            }
            binding.shareAppRow.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Create with LumoraAI: $appShareUrl")
                }
                root.context.startActivity(Intent.createChooser(shareIntent, root.context.getString(R.string.ui_share_app)))
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF081020))
            .statusBarsPadding()
            .navigationBarsPadding(),
    )
}
