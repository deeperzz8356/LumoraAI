package com.deep.lumoraai.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
private val Purple = Color(0xFF9C63FF)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SettingsBackground,
        topBar = {
            AppToolbar(
                title = stringResource(com.deep.lumoraai.R.string.ui_help_support),
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
                    "Get in Touch",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                SupportContactCard(
                    icon = Icons.Default.Email,
                    title = stringResource(com.deep.lumoraai.R.string.ui_email_support),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_support_lumora_ai),
                    description = "Send us an email and we'll respond within 24 hours",
                    iconTint = Lime,
                    onClick = { }
                )

                SupportContactCard(
                    icon = Icons.Default.Phone,
                    title = stringResource(com.deep.lumoraai.R.string.ui_live_chat),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_available_24_7),
                    description = "Chat with our support team in real-time",
                    iconTint = Cyan,
                    onClick = { }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Resources",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                SupportResourceCard(
                    icon = Icons.Default.Help,
                    title = stringResource(com.deep.lumoraai.R.string.ui_faq_knowledge_base),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_find_answers_to_common_questions),
                    iconTint = Purple,
                    onClick = { }
                )

                SupportResourceCard(
                    icon = Icons.Default.Help,
                    title = stringResource(com.deep.lumoraai.R.string.ui_video_tutorials),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_learn_how_to_use_lumora_ai_features),
                    iconTint = Lime,
                    onClick = { }
                )

                SupportResourceCard(
                    icon = Icons.Default.Help,
                    title = stringResource(com.deep.lumoraai.R.string.ui_status_page),
                    subtitle = stringResource(com.deep.lumoraai.R.string.ui_check_service_status_and_updates),
                    iconTint = Cyan,
                    onClick = { }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Report an Issue",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Surface(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    color = SettingsCard,
                    border = BorderStroke(1.dp, SettingsStroke.copy(alpha = 0.72f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Report a Bug",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tell us about any issues you've encountered so we can fix them quickly",
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                        Text(
                            "→",
                            style = MaterialTheme.typography.titleLarge,
                            color = Lime,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Contact Info",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                ContactInfoRow(label = "Email:", value = "support@lumora.ai")
                ContactInfoRow(label = "Phone:", value = "+1 (555) 123-4567")
                ContactInfoRow(label = "Hours:", value = "24/7 Support Available")
            }
        }
    }
}

@Composable
private fun SupportContactCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    description: String,
    iconTint: Color,
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = iconTint, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }
        }
    }
}

@Composable
private fun SupportResourceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint
                )
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
                }
            }
            Text(">", style = MaterialTheme.typography.titleLarge, color = Lime, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ContactInfoRow(label: String, value: String) {
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
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = Muted,
                fontWeight = FontWeight.Bold
            )
            Text(value, style = MaterialTheme.typography.titleMedium, color = Lime, fontWeight = FontWeight.Bold)
        }
    }
}
