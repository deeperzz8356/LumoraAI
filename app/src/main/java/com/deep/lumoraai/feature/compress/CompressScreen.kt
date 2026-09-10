package com.deep.lumoraai.feature.compress

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.deep.lumoraai.ads.AdPlacement
import com.deep.lumoraai.ads.PlacementNativeAd
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.utils.CreditBalanceStore
import com.deep.lumoraai.databinding.CompressScreenBinding

@Composable
fun CompressScreen(
    uiState: CompressUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onFileSelected: (Uri) -> Unit,
    onCompress: () -> Unit,
    onDownload: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val credits by CreditBalanceStore.balance.collectAsState()
    var permissionDenied by remember { mutableStateOf(false) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onFileSelected(uri)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.all { it }) {
            permissionDenied = false
            filePicker.launch("*/*")
        } else {
            permissionDenied = true
        }
    }
    val openPickerWithPermission = {
        val permissions = mediaReadPermissions()
        val hasPermission = permissions.isEmpty() || permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (hasPermission) {
            permissionDenied = false
            filePicker.launch("*/*")
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF081020))
            .systemBarsPadding()
    ) {
        AndroidView(
            factory = { CompressScreenBinding.inflate(LayoutInflater.from(it)).root },
            update = { root ->
                bindCompress(
                    binding = CompressScreenBinding.bind(root),
                    uiState = uiState,
                    credits = credits ?: 0,
                    permissionDenied = permissionDenied,
                    onBack = onBack,
                    onNavigate = onNavigate,
                    onOpenPicker = openPickerWithPermission,
                    onCompress = onCompress,
                    onDownload = onDownload,
                    onReset = onReset,
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        )

        PlacementNativeAd(
            placement = AdPlacement.NATIVE_COMPRESS,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

private fun bindCompress(
    binding: CompressScreenBinding,
    uiState: CompressUiState,
    credits: Int,
    permissionDenied: Boolean,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenPicker: () -> Unit,
    onCompress: () -> Unit,
    onDownload: () -> Unit,
    onReset: () -> Unit,
) {
    binding.backButton.setOnClickListener { onBack() }
    binding.creditsChip.text = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        credits.toString()
    }
    binding.creditsChip.setOnClickListener { onNavigate(Screen.Credits.route) }
    binding.notificationButton.setOnClickListener { onNavigate(Screen.Notifications.route) }
    binding.unreadDot.visibility = View.GONE

    val hasResult = uiState.result != null
    binding.uploadSection.visibility = if (hasResult) View.GONE else View.VISIBLE
    binding.resultSection.visibility = if (hasResult) View.VISIBLE else View.GONE
    binding.permissionMessage.visibility = if (permissionDenied && !hasResult) View.VISIBLE else View.GONE

    binding.uploadPanel.isEnabled = !uiState.isCompressing
    binding.uploadPanel.setOnClickListener { onOpenPicker() }
    binding.fileName.text = uiState.fileName.ifBlank { "Drag and drop or tap to select a file" }
    binding.fileName.setTextColor(if (uiState.fileName.isBlank()) 0xFF9BA6BA.toInt() else 0xFFFFFFFF.toInt())
    binding.fileName.textSize = if (uiState.fileName.isBlank()) 13f else 15f
    binding.fileType.visibility = if (uiState.fileName.isBlank()) View.GONE else View.VISIBLE
    binding.fileType.text = if (uiState.mimeType.startsWith("video/")) "Video selected" else "Image selected"

    binding.compressButton.isEnabled = uiState.selectedUri != null && !uiState.isCompressing
    binding.compressButton.text = if (uiState.isCompressing) "Compressing..." else "Compress Now"
    binding.compressButton.setOnClickListener { onCompress() }

    uiState.result?.let { result ->
        binding.originalSize.text = formatBytes(result.originalBytes)
        binding.compressedSize.text = formatBytes(result.compressedBytes)
        binding.savedSize.text = "${savedPercent(result)}%"
    }
    binding.downloadButton.setOnClickListener { onDownload() }
    binding.resetButton.setOnClickListener { onReset() }
    binding.downloadMessage.visibility = if (uiState.downloadMessage == null) View.GONE else View.VISIBLE
    binding.downloadMessage.text = uiState.downloadMessage.orEmpty()

    binding.errorText.visibility = if (uiState.error == null) View.GONE else View.VISIBLE
    binding.errorText.text = uiState.error.orEmpty()
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 KB"
    val mb = bytes / (1024f * 1024f)
    return if (mb >= 1f) {
        String.format("%.1f MB", mb)
    } else {
        String.format("%.0f KB", bytes / 1024f)
    }
}

private fun savedPercent(result: CompressionResult): Int {
    if (result.originalBytes <= 0L) return 0
    val saved = ((result.originalBytes - result.compressedBytes).coerceAtLeast(0L) * 100f) / result.originalBytes
    return saved.toInt().coerceIn(0, 100)
}

private fun mediaReadPermissions(): Array<String> =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
        else -> emptyArray()
    }
