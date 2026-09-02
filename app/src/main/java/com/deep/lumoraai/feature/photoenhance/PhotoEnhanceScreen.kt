package com.deep.lumoraai.feature.photoenhance

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.LumoraCreditsChip
import com.deep.lumoraai.core.components.LumoraNotificationBell
import com.deep.lumoraai.core.navigation.Screen

private val EnhanceBackground = Color(0xFF081020)
private val EnhancePanel = Color(0xFF121A2E)
private val EnhanceStroke = Color(0xFF26364F)
private val Lime = Color(0xFFD6FF2F)
private val Muted = Color(0xFF9BA6BA)

@Composable
fun PhotoEnhanceScreen(
    uiState: PhotoEnhanceUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onResolutionSelected: (EnhanceOption) -> Unit,
    onSharpnessChanged: (Float) -> Unit,
    onLightingSelected: (EnhanceOption) -> Unit,
    onEnhance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageSelected(uri)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EnhanceBackground)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            EnhanceTopBar(
                credits = uiState.credits,
                onBack = onBack,
                onCredits = { onNavigate(Screen.Credits.route) },
                onNotifications = { onNavigate(Screen.Notifications.route) }
            )

            UploadPanel(
                uiState = uiState,
                onUploadClick = { imagePicker.launch("image/*") }
            )

            EnhancementControls(
                resolution = uiState.resolution,
                sharpness = uiState.sharpness,
                lighting = uiState.lighting,
                onResolutionSelected = onResolutionSelected,
                onSharpnessChanged = onSharpnessChanged,
                onLightingSelected = onLightingSelected
            )

            Button(
                onClick = onEnhance,
                enabled = uiState.originalBitmap != null && !uiState.isEnhancing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Lime,
                    disabledContainerColor = Lime.copy(alpha = 0.35f)
                )
            ) {
                if (uiState.isEnhancing) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Enhance Now",
                        color = Color.Black,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            ResultPanel(uiState = uiState)

            if (uiState.enhancedBitmap != null) {
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !uiState.isEnhancing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        disabledContainerColor = Color.White.copy(alpha = 0.03f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enhance Another Image",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhanceTopBar(
    credits: Int,
    onBack: () -> Unit,
    onCredits: () -> Unit,
    onNotifications: () -> Unit,
    hasUnreadNotifications: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Photo Enhancer",
                color = Color.White,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            LumoraCreditsChip(credits = credits, onClick = onCredits)
            LumoraNotificationBell(
                hasUnreadNotifications = hasUnreadNotifications,
                onClick = onNotifications
            )
        }
    }
}

@Composable
private fun UploadPanel(
    uiState: PhotoEnhanceUiState,
    onUploadClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(254.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(EnhancePanel.copy(alpha = 0.55f))
            .border(BorderStroke(1.dp, EnhanceStroke.copy(alpha = 0.78f)), RoundedCornerShape(14.dp))
            .clickable(enabled = !uiState.isEnhancing, onClick = onUploadClick),
        contentAlignment = Alignment.Center
    ) {
        val preview = uiState.enhancedBitmap ?: uiState.originalBitmap
        if (uiState.originalBitmap != null && uiState.enhancedBitmap != null) {
            BeforeAfterPreview(
                original = uiState.originalBitmap,
                enhanced = uiState.enhancedBitmap,
                modifier = Modifier.fillMaxSize()
            )
        } else if (preview != null) {
            Image(
                bitmap = preview.asImageBitmap(),
                contentDescription = "Selected image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.52f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (uiState.enhancedBitmap != null) "Enhanced" else "Original",
                    color = if (uiState.enhancedBitmap != null) Lime else Color.White,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))))
                    .padding(12.dp)
            ) {
                Text(
                    text = if (uiState.enhancedBitmap != null) "Enhanced preview" else "Tap to choose another image",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Upload Image",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Drag and drop or tap to select a file",
                    color = Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BeforeAfterPreview(
    original: android.graphics.Bitmap,
    enhanced: android.graphics.Bitmap,
    modifier: Modifier = Modifier,
) {
    var reveal by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    reveal = (offset.x / size.width).coerceIn(0.05f, 0.95f)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    reveal = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                }
            }
    ) {
        Image(
            bitmap = original.asImageBitmap(),
            contentDescription = "Original image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            bitmap = enhanced.asImageBitmap(),
            contentDescription = "Enhanced image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    clipRect(right = size.width * reveal) {
                        this@drawWithContent.drawContent()
                    }
                }
        )

        ComparisonPill(
            text = "Enhanced",
            selected = true,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        )
        ComparisonPill(
            text = "Original",
            selected = false,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = maxWidth * reveal)
                .width(2.dp)
                .fillMaxHeight()
                .background(Lime)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = maxWidth * reveal - 18.dp)
                .size(width = 36.dp, height = 52.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.62f))
                .border(1.dp, Lime.copy(alpha = 0.85f), RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↔",
                color = Lime,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))))
                .padding(12.dp)
        ) {
            Text(
                text = "Slide to compare",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ComparisonPill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.52f))
            .border(1.dp, if (selected) Lime.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Lime else Color.White,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EnhancementControls(
    resolution: EnhanceOption,
    sharpness: Float,
    lighting: EnhanceOption,
    onResolutionSelected: (EnhanceOption) -> Unit,
    onSharpnessChanged: (Float) -> Unit,
    onLightingSelected: (EnhanceOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(EnhancePanel.copy(alpha = 0.82f))
            .border(1.dp, EnhanceStroke.copy(alpha = 0.64f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Lime.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Enhancement", color = Color.White, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
                Text("Sharper detail, cleaner tone, richer color", color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
        OptionSection(
            title = "Resolution",
            selected = resolution,
            onSelected = onResolutionSelected
        )
        SharpnessSection(
            sharpness = sharpness,
            onSharpnessChanged = onSharpnessChanged
        )
        OptionSection(
            title = "Lighting",
            selected = lighting,
            onSelected = onLightingSelected
        )
    }
}

@Composable
private fun OptionSection(
    title: String,
    selected: EnhanceOption,
    onSelected: (EnhanceOption) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = Color.White, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF171F33))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EnhanceOption.entries.forEach { option ->
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Lime else Color.Transparent)
                        .clickable { onSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.label,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SharpnessSection(
    sharpness: Float,
    onSharpnessChanged: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Detail Recovery", color = Color.White, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
            Text("${(sharpness * 100).toInt()}%", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = sharpness,
            onValueChange = onSharpnessChanged,
            colors = SliderDefaults.colors(
                thumbColor = Lime,
                activeTrackColor = Lime,
                inactiveTrackColor = Color(0xFF172033)
            )
        )
    }
}

@Composable
private fun ResultPanel(uiState: PhotoEnhanceUiState) {
    when {
        uiState.error != null -> {
            Text(
                text = uiState.error,
                color = Color(0xFFFF7A7A),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        uiState.savedPath != null -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF111A2D))
                    .border(1.dp, Lime.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Lime, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Enhanced image saved to History",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
