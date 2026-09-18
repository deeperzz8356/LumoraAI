package com.deep.lumoraai.feature.uninstall

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.R
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementNativeAd

@Composable
fun UninstallConfirmRoute(
    onBackHome: () -> Unit,
    onStillUninstall: () -> Unit,
) {
    BackHandler(onBack = onBackHome)
    UninstallScaffold(
        title = stringResource(R.string.ui_uninstall_v2_confirm_title),
        subtitle = stringResource(R.string.ui_uninstall_v2_confirm_subtitle),
        onBackHome = onBackHome,
    ) {
        Text(
            text = stringResource(R.string.ui_uninstall_v2_confirm_body),
            color = Color(0xFFB8C2D8),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.ui_uninstall_v2_confirm_body_secondary),
            color = Color(0xFFB8C2D8),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBackHome,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD6FF2F),
                contentColor = Color(0xFF081020),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.ui_try_again), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = onStillUninstall,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.ui_uninstall_v2_still), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UninstallSurveyRoute(onBack: () -> Unit, onBackHome: () -> Unit) {
    val context = LocalContext.current
    val openAppDetails = {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")))
        }.onFailure { android.util.Log.w("Uninstall", "Unable to open application details", it) }
        Unit
    }
    val reasons = listOf(
        stringResource(R.string.ui_uninstall_v2_reason_features),
        stringResource(R.string.ui_uninstall_reason_ads),
        stringResource(R.string.ui_uninstall_v2_reason_not_using),
        stringResource(R.string.ui_uninstall_v2_reason_other),
    )
    var selectedIndex by rememberSaveable { mutableStateOf(1) }
    var otherReason by rememberSaveable { mutableStateOf("") }
    BackHandler(onBack = onBack)

    UninstallScaffold(
        title = stringResource(R.string.ui_uninstall_v2_survey_title),
        subtitle = stringResource(R.string.ui_uninstall_v2_survey_subtitle),
        onBackHome = onBack,
    ) {
        reasons.forEachIndexed { index, reason ->
            ReasonRow(
                text = reason,
                selected = selectedIndex == index,
                onClick = { selectedIndex = index },
            )
            Spacer(Modifier.height(10.dp))
        }
        if (selectedIndex == 3) {
            OutlinedTextField(
                value = otherReason,
                onValueChange = { otherReason = it },
                label = { Text(stringResource(R.string.ui_uninstall_v2_other_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onBackHome,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD6FF2F)),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.ui_cancel), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = openAppDetails,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B6B),
                    contentColor = Color.White,
                ),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.ui_uninstall), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UninstallScaffold(
    title: String,
    subtitle: String,
    onBackHome: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF081020))
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackHome) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.ui_back), tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Color(0xFF94A0B8), style = MaterialTheme.typography.bodySmall)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
        Box(Modifier.fillMaxWidth().navigationBarsPadding()) {
            PlacementNativeAd(
                placement = AdPlacement.NATIVE_UNINSTALL,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ReasonRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0E172A))
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFFD6FF2F) else Color(0x1FFFFFFF),
                shape = RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFFD6FF2F),
                unselectedColor = Color(0xFF94A0B8),
            ),
        )
        Text(text, color = Color.White, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color(0xFFD6FF2F))
        }
    }
}
