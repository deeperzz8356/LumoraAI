package com.deep.lumoraai.feature.uninstall

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
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
        title = stringResource(R.string.ui_uninstall_confirm_title),
        subtitle = stringResource(R.string.ui_uninstall_confirm_subtitle),
        onBackHome = onBackHome,
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(Color(0x22FF6B6B)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.DeleteForever,
                contentDescription = null,
                tint = Color(0xFFFF6B6B),
                modifier = Modifier.size(42.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.ui_uninstall_confirm_body),
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
            Text(stringResource(R.string.ui_uninstall_no), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = onStillUninstall,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.ui_uninstall_yes), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UninstallSurveyRoute(onBackHome: () -> Unit) {
    val context = LocalContext.current
    val openAppDetails = {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")))
        }.onFailure { android.util.Log.w("Uninstall", "Unable to open application details", it) }
        Unit
    }
    val uninstallLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_FIRST_USER) openAppDetails()
    }
    val reasons = listOf(
        stringResource(R.string.ui_uninstall_reason_not_using),
        stringResource(R.string.ui_uninstall_reason_ads),
        stringResource(R.string.ui_uninstall_reason_cost),
        stringResource(R.string.ui_uninstall_reason_quality),
        stringResource(R.string.ui_uninstall_reason_other),
    )
    var selectedIndex by rememberSaveable { mutableStateOf(-1) }
    BackHandler(onBack = onBackHome)

    UninstallScaffold(
        title = stringResource(R.string.ui_uninstall_survey_title),
        subtitle = stringResource(R.string.ui_uninstall_survey_subtitle),
        onBackHome = onBackHome,
    ) {
        reasons.forEachIndexed { index, reason ->
            ReasonRow(
                text = reason,
                selected = selectedIndex == index,
                onClick = { selectedIndex = index },
            )
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = selectedIndex in reasons.indices,
            onClick = {
                if (selectedIndex !in reasons.indices) return@Button
                runCatching {
                    uninstallLauncher.launch(
                        Intent(Intent.ACTION_UNINSTALL_PACKAGE,
                            Uri.parse("package:${context.packageName}"))
                            .putExtra(Intent.EXTRA_RETURN_RESULT, true)
                    )
                }.onFailure { openAppDetails() }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6B6B),
                contentColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.ui_uninstall), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = onBackHome,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD6FF2F)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.ui_try_again), fontWeight = FontWeight.Bold)
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
            .systemBarsPadding(),
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
        PlacementNativeAd(
            placement = AdPlacement.NATIVE_UNINSTALL,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
        )
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
