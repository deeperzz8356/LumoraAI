package com.deep.lumoraai.feature.imagetoimage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.navigation.Screen

private val ScreenBg = Color(0xFF081020)
private val Panel = Color(0xFF151D31)
private val Stroke = Color(0xFF26364F)
private val Lime = Color(0xFFD6FF2F)
private val Muted = Color(0xFF9AA5B8)

@Composable
fun ImageToImageScreen(
    uiState: ImageToImageUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onPromptChanged: (String) -> Unit,
    onStyleSelected: (ImageStyle) -> Unit,
    onSimilarityChanged: (Float) -> Unit,
    onGenerationsChanged: (Int) -> Unit,
    onGenerate: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onImageSelected(uri)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                onBack = onBack,
                onNotifications = { onNavigate(Screen.Notifications.route) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                UploadPanel(uiState = uiState, onUpload = { imagePicker.launch("image/*") })
                PromptPanel(
                    prompt = uiState.prompt,
                    onPromptChanged = onPromptChanged,
                    onUpload = { imagePicker.launch("image/*") }
                )
                StyleSection(selected = uiState.selectedStyle, onSelected = onStyleSelected)
                ControlsPanel(
                    similarity = uiState.similarity,
                    generations = uiState.generations,
                    onSimilarityChanged = onSimilarityChanged,
                    onGenerationsChanged = onGenerationsChanged
                )
                Button(
                    onClick = onGenerate,
                    enabled = !uiState.isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Lime,
                        disabledContainerColor = Lime.copy(alpha = 0.38f)
                    )
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Text("✦ Generate Now", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                if (uiState.error != null) {
                    Text(
                        text = uiState.error,
                        color = Color(0xFFFF7A7A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onDismissError)
                    )
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    onNotifications: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFF0B1426))
            .border(1.dp, Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 20.dp),
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
                text = "Image 2 Image",
                color = Color.White,
                fontSize = 21.sp,
                lineHeight = 25.sp,
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
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFFDFF7F4), modifier = Modifier.size(20.dp))
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
private fun UploadPanel(uiState: ImageToImageUiState, onUpload: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Panel.copy(alpha = 0.55f))
            .border(BorderStroke(2.dp, Color(0xFF566B58)), RoundedCornerShape(16.dp))
            .clickable(enabled = !uiState.isGenerating, onClick = onUpload),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = uiState.sourceBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Source image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color(0xFF111A2D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Lime, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text("Upload Image", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Drag and drop or tap to select a file",
                    color = Muted,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
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
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
    ) {
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChanged,
            placeholder = {
                Text(
                    text = "Describe the image you want to generate...",
                    color = Muted.copy(alpha = 0.65f),
                    fontSize = 18.sp,
                    lineHeight = 26.sp
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 70.dp),
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
                .padding(start = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SquareButton(icon = Icons.Default.Upload, onClick = onUpload)
            SquareButton(icon = Icons.Default.Tune, onClick = {})
        }
        Text(
            text = "${prompt.length}/1000",
            color = Color.White.copy(alpha = 0.78f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
        )
    }
}

@Composable
private fun SquareButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1B263B))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun StyleSection(selected: ImageStyle, onSelected: (ImageStyle) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Style", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("More ›", color = Lime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ImageStyle.entries.forEach { style ->
                val isSelected = selected == style
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) Lime.copy(alpha = 0.16f) else Panel)
                        .border(1.dp, if (isSelected) Lime else Color.Transparent, RoundedCornerShape(50))
                        .clickable { onSelected(style) }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = style.label,
                        color = if (isSelected) Lime else Color.White.copy(alpha = 0.72f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            StyleCard(R.drawable.style_anime, selected = selected == ImageStyle.All, onClick = { onSelected(ImageStyle.All) })
            StyleCard(R.drawable.style_digital, selected = selected == ImageStyle.MyStyle, onClick = { onSelected(ImageStyle.MyStyle) })
            StyleCard(R.drawable.style_fantasy, selected = selected == ImageStyle.Concept, onClick = { onSelected(ImageStyle.Concept) })
        }
    }
}

@Composable
private fun StyleCard(imageRes: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(124.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(9.dp))
            .border(2.dp, if (selected) Lime else Color.Transparent, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(30.dp)
                    .background(Lime, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun ControlsPanel(
    similarity: Float,
    generations: Int,
    onSimilarityChanged: (Float) -> Unit,
    onGenerationsChanged: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Panel)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Image Similarity", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("${(similarity * 100).toInt()}%", color = Lime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = similarity,
            onValueChange = onSimilarityChanged,
            colors = SliderDefaults.colors(
                thumbColor = Lime,
                activeTrackColor = Lime.copy(alpha = 0.58f),
                inactiveTrackColor = Color.White.copy(alpha = 0.13f)
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("No. of Generations", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF182137))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(50))
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Text("−", color = Color.White.copy(alpha = 0.72f), fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onGenerationsChanged(generations - 1) })
                Text("$generations", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("+", color = Lime, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onGenerationsChanged(generations + 1) })
            }
        }
    }
}
