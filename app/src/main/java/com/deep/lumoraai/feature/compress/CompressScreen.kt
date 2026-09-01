package com.deep.lumoraai.feature.compress

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.navigation.Screen

private val CompressBackground = Color(0xFF081020)
private val CompressPanel = Color(0xFF121A2E)
private val CompressStroke = Color(0xFF25354C)
private val Lime = Color(0xFFD6FF2F)
private val Muted = Color(0xFF9BA6BA)

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
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onFileSelected(uri)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CompressBackground)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CompressTopBar(
                onBack = onBack,
                onNotifications = { onNavigate(Screen.Notifications.route) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 15.dp)
                    .padding(top = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (uiState.result == null) {
                    UploadPanel(
                        uiState = uiState,
                        onClick = { filePicker.launch("*/*") }
                    )

                    Button(
                        onClick = onCompress,
                        enabled = uiState.selectedUri != null && !uiState.isCompressing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Lime,
                            disabledContainerColor = Lime.copy(alpha = 0.35f)
                        )
                    ) {
                        if (uiState.isCompressing) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Compress Now",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    CompletionPanel(
                        uiState = uiState,
                        result = uiState.result,
                        onDownload = onDownload,
                        onReset = onReset,
                    )
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = Color(0xFFFF7A7A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (uiState.result == null) {
                    Spacer(modifier = Modifier.height(300.dp))
                }
            }
        }
    }
}

@Composable
private fun CompressTopBar(
    onBack: () -> Unit,
    onNotifications: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF0B1426))
            .border(1.dp, Color(0xFF0D8BFF))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Compress",
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onNotifications),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFFDFF7F4),
                modifier = Modifier.size(18.dp)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .align(Alignment.TopEnd)
                    .background(Lime, CircleShape)
            )
        }
    }
}

@Composable
private fun UploadPanel(
    uiState: CompressUiState,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(235.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(CompressPanel.copy(alpha = 0.55f))
            .border(BorderStroke(1.dp, CompressStroke), RoundedCornerShape(9.dp))
            .clickable(enabled = !uiState.isCompressing, onClick = onClick)
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(Color(0xFF10192D), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Lime,
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = uiState.fileName.ifBlank { "Drag and drop or tap to select a file" },
                color = if (uiState.fileName.isBlank()) Muted else Color.White,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.fileName.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (uiState.mimeType.startsWith("video/")) "Video selected" else "Image selected",
                    color = Muted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CompletionPanel(
    uiState: CompressUiState,
    result: CompressionResult,
    onDownload: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .background(Lime.copy(alpha = 0.14f), CircleShape)
                .border(1.dp, Lime.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Lime, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(25.dp))
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Compression Complete",
                color = Color.White,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your file has been optimized successfully.",
                color = Muted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        StatsCard(result = result)

        Button(
            onClick = onDownload,
            modifier = Modifier
                .fillMaxWidth()
                .height(49.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Lime)
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Download Now", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }

        if (uiState.downloadMessage != null) {
            Text(
                text = uiState.downloadMessage,
                color = Lime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(47.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, CompressStroke)
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compress Another", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatsCard(result: CompressionResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CompressPanel.copy(alpha = 0.75f))
            .border(1.dp, CompressStroke.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatRow("Original Size", formatBytes(result.originalBytes), Color.White)
        StatRow("Compressed Size", formatBytes(result.compressedBytes), Lime)
        StatRow("Space Saved", "${savedPercent(result)}%", Lime, pill = true)
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color, pill: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Muted, fontSize = 12.sp)
        if (pill) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Lime.copy(alpha = 0.12f))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(value, color = valueColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
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
