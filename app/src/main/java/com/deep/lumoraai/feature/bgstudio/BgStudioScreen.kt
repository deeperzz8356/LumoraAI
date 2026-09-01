package com.deep.lumoraai.feature.bgstudio

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate

private val StudioBackground = Color(0xFF081020)
private val StudioPanel = Color(0xFF121A2E)
private val StudioField = Color(0xFF151D31)
private val StudioStroke = Color(0xFF25344C)
private val Lime = Color(0xFFD6FF2F)
private val Muted = Color(0xFF9AA5B8)

@Composable
fun BgStudioScreen(
    uiState: BgStudioUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onModeSelected: (BgStudioMode) -> Unit,
    onPromptChanged: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onCreate: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageSelected(uri)
    }
    val isBusy = uiState.status == BgStudioStatus.LoadingImage || uiState.status == BgStudioStatus.Generating

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            StudioTopBar(
                onBack = onBack,
                onCredits = { onNavigate(Screen.Credits.route) },
                onNotifications = { onNavigate(Screen.Notifications.route) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ModeSwitch(selectedMode = uiState.mode, onModeSelected = onModeSelected)

                if (uiState.mode == BgStudioMode.Replace) {
                    SourceImagePanel(uiState = uiState, onUpload = { imagePicker.launch("image/*") })
                    PromptPanel(
                        prompt = uiState.prompt,
                        onPromptChanged = onPromptChanged,
                        onUpload = { imagePicker.launch("image/*") }
                    )
                    StudioPrimaryButton(text = "CREATE ϟ", isBusy = isBusy, enabled = !isBusy, onClick = onCreate)
                } else {
                    RemoveBackgroundPanel(uiState = uiState, onUpload = { imagePicker.launch("image/*") })
                    StudioPrimaryButton(text = "✂  REMOVE BACKGROUND", isBusy = isBusy, enabled = !isBusy, onClick = onCreate)
                }

                StatusMessage(status = uiState.status, onDismissError = onDismissError)
            }
        }
    }
}

@Composable
private fun StudioTopBar(
    onBack: () -> Unit,
    onCredits: () -> Unit,
    onNotifications: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF0B1426))
            .border(1.dp, Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 19.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(23.dp)
                    .clickable(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Bg Studio",
                color = Color.White,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CreditsPill(credits = 1250, onClick = onCredits)
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
                    modifier = Modifier.size(20.dp)
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
}

@Composable
private fun CreditsPill(credits: Int, onClick: () -> Unit) {
    val label = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) "Unlimited" else "$credits"
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("◉", color = Lime, fontSize = 10.sp, lineHeight = 10.sp)
        Text(label, color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ModeSwitch(selectedMode: BgStudioMode, onModeSelected: (BgStudioMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(StudioPanel)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BgStudioMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) Lime else Color.Transparent)
                    .clickable { onModeSelected(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    color = if (selected) Color.Black else Muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SourceImagePanel(uiState: BgStudioUiState, onUpload: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(293.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudioPanel)
            .border(BorderStroke(1.dp, StudioStroke), RoundedCornerShape(8.dp))
            .clickable(onClick = onUpload),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = uiState.sourceBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Source image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.style_digital),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.26f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Lime, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Upload source image", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RemoveBackgroundPanel(uiState: BgStudioUiState, onUpload: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(335.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(StudioPanel)
            .border(BorderStroke(1.dp, StudioStroke), RoundedCornerShape(8.dp))
            .clickable(onClick = onUpload),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = uiState.sourceBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Source subject",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.style_fantasy),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.62f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("✦", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (bitmap == null) "Tap to select subject" else "Subject Detected",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PromptPanel(
    prompt: String,
    onPromptChanged: (String) -> Unit,
    onUpload: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(StudioField)
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(9.dp))
    ) {
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChanged,
            placeholder = {
                Text(
                    text = "Describe the image you want to generate...",
                    color = Muted,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Lime
            )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            SquareToolButton(icon = Icons.Default.Upload, onClick = onUpload)
            SquareToolButton(icon = Icons.Default.Tune, onClick = {})
        }
        Text(
            text = "${prompt.length}/1000",
            color = Color.White.copy(alpha = 0.74f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 14.dp)
        )
    }
}

@Composable
private fun StudioPrimaryButton(
    text: String,
    isBusy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp),
        shape = RoundedCornerShape(9.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Lime,
            disabledContainerColor = Lime.copy(alpha = 0.35f)
        )
    ) {
        if (isBusy) {
            CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        } else {
            Text(
                text = text,
                color = Color.Black,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SquareToolButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF202A3F))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun StatusMessage(status: BgStudioStatus, onDismissError: () -> Unit) {
    if (status is BgStudioStatus.Error) {
        Text(
            text = status.message,
            color = Color(0xFFFF7A7A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onDismissError)
        )
    }
}
