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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import androidx.compose.ui.res.stringResource

private val SettingsBackground = Color(0xFF081020)
private val SettingsCard = Color(0xFF10192D)
private val SettingsStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun PrivacySecurityScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dataCollection by remember { mutableStateOf(true) }
    var thirdPartySharing by remember { mutableStateOf(false) }
    var twoFactorAuth by remember { mutableStateOf(true) }
    var sessionTimeout by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SettingsBackground,
        topBar = {
            AppToolbar(
                title = stringResource(com.deep.lumoraai.R.string.ui_privacy_security),
                action = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(com.deep.lumoraai.R.string.ui_back),
                            tint = Color.White
                        )
                    }
                }
            )
        },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Privacy Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                PrivacyToggleRow(
                    title = stringResource(com.deep.lumoraai.R.string.ui_data_collection),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_allow_us_to_collect_usage_data_to_improve_your_experience),
                    checked = dataCollection,
                    onCheckedChange = { dataCollection = it }
                )

                PrivacyToggleRow(
                    title = stringResource(com.deep.lumoraai.R.string.ui_third_party_sharing),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_share_your_data_with_partner_services),
                    checked = thirdPartySharing,
                    onCheckedChange = { thirdPartySharing = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Security Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                PrivacyToggleRow(
                    title = stringResource(com.deep.lumoraai.R.string.ui_two_factor_authentication),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_add_an_extra_layer_of_security_to_your_account),
                    checked = twoFactorAuth,
                    onCheckedChange = { twoFactorAuth = it }
                )

                PrivacyToggleRow(
                    title = stringResource(com.deep.lumoraai.R.string.ui_session_timeout),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_automatically_log_out_after_30_minutes_of_inactivity),
                    checked = sessionTimeout,
                    onCheckedChange = { sessionTimeout = it }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Data Management",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                PrivacyActionRow(
                    title = stringResource(com.deep.lumoraai.R.string.ui_download_your_data),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_export_all_your_personal_data),
                    onClick = { }
                )

                PrivacyActionRow(
                    title = stringResource(com.deep.lumoraai.R.string.ui_view_privacy_policy),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_read_our_complete_privacy_policy),
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun PrivacyToggleRow(
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
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
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
private fun PrivacyActionRow(
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
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            Text(">", style = MaterialTheme.typography.titleLarge, color = Lime, fontWeight = FontWeight.Bold)
        }
    }
}
