package com.deep.lumoraai.feature.generation

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.feature.imagetoimage.ImageStyle
import com.deep.lumoraai.feature.imagetoimage.VideoStyle

val GenerationScreenBg = Color(0xFF081020)
val GenerationPanel = Color(0xFF151D31)
val GenerationLime = Color(0xFFD6FF2F)
val GenerationMuted = Color(0xFF9AA5B8)

@Composable
fun GenerationTopBar(
    title: String,
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Color(0xFF0B1426))
            .border(1.dp, Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
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
                .size(34.dp)
                .clickable(onClick = onNotifications),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color(0xFFDFF7F4),
                modifier = Modifier.size(20.dp)
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .align(Alignment.TopEnd)
                    .background(GenerationLime, CircleShape)
            )
        }
    }
}

@Composable
fun UploadImagePanel(
    bitmap: Bitmap?,
    isBusy: Boolean,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GenerationPanel.copy(alpha = 0.55f))
            .border(BorderStroke(1.5.dp, Color(0xFF516759)), RoundedCornerShape(16.dp))
            .clickable(enabled = !isBusy, onClick = onUpload),
        contentAlignment = Alignment.Center
    ) {
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
                        .size(62.dp)
                        .background(Color(0xFF111A2D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = GenerationLime, modifier = Modifier.size(27.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Upload Image", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "Tap to select a file from your device",
                    color = GenerationMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PromptComposerCard(
    prompt: String,
    promptHint: String,
    negativePrompt: String,
    isImproving: Boolean,
    onPromptChanged: (String) -> Unit,
    onImprovePrompt: () -> Unit,
    onNegativePromptChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    onUpload: (() -> Unit)? = null,
) {
    var showNegativePrompt by remember { mutableStateOf(false) }
    if (showNegativePrompt) {
        NegativePromptDialog(
            value = negativePrompt,
            onValueChange = onNegativePromptChanged,
            onDismiss = { showNegativePrompt = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GenerationPanel)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
    ) {
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChanged,
            placeholder = {
                Text(
                    text = promptHint,
                    color = GenerationMuted.copy(alpha = 0.68f),
                    fontSize = 17.sp,
                    lineHeight = 25.sp
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp)
                .padding(bottom = 66.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = GenerationLime
            )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onUpload != null) {
                SquareActionButton(icon = Icons.Default.Upload, contentDescription = "Upload image", onClick = onUpload)
            }
            SquareActionButton(
                icon = Icons.Default.AutoAwesome,
                contentDescription = "Improve prompt",
                enabled = prompt.isNotBlank() && !isImproving,
                onClick = onImprovePrompt,
                isLoading = isImproving
            )
            SquareActionButton(
                icon = Icons.Default.Tune,
                contentDescription = "Negative prompt",
                onClick = { showNegativePrompt = true },
                highlighted = negativePrompt.isNotBlank()
            )
        }
        Text(
            text = "${prompt.length}/1000",
            color = Color.White.copy(alpha = 0.78f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 23.dp)
        )
    }
}

@Composable
private fun SquareActionButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    highlighted: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) GenerationLime.copy(alpha = 0.15f) else Color(0xFF1B263B))
            .border(1.dp, if (highlighted) GenerationLime.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = GenerationLime, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = if (enabled) GenerationLime else Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun NegativePromptDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(value) { mutableStateOf(value) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GenerationPanel,
        titleContentColor = Color.White,
        textContentColor = GenerationMuted,
        title = {
            Text("Negative Prompt", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tell Lumora what to avoid in this generation.", fontSize = 13.sp)
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(1000) },
                    placeholder = { Text("blurry, extra fingers, bad anatomy, watermark...") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF10182A),
                        unfocusedContainerColor = Color(0xFF10182A),
                        focusedBorderColor = GenerationLime.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = GenerationLime
                    )
                )
                Text("${draft.length}/1000", fontSize = 12.sp, color = Color.White.copy(alpha = 0.56f), modifier = Modifier.align(Alignment.End))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onValueChange(draft)
                onDismiss()
            }) {
                Text("Save", color = GenerationLime, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GenerationMuted)
            }
        }
    )
}

@Composable
fun ImageStyleSection(
    selected: ImageStyle,
    onSelected: (ImageStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    StyleSection(
        title = "Style",
        items = ImageStyle.entries.map { StyleItem(it.label, it.promptHint, selected == it) { onSelected(it) } },
        modifier = modifier
    )
}

@Composable
fun VideoStyleSection(
    selected: VideoStyle,
    onSelected: (VideoStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    StyleSection(
        title = "Style",
        items = VideoStyle.entries.map { StyleItem(it.label, it.promptHint, selected == it) { onSelected(it) } },
        modifier = modifier
    )
}

private data class StyleItem(
    val label: String,
    val hint: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun StyleSection(
    title: String,
    items: List<StyleItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { item ->
                StyleCard(item = item)
            }
        }
    }
}

@Composable
private fun StyleCard(item: StyleItem) {
    Column(
        modifier = Modifier
            .width(178.dp)
            .height(92.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (item.selected) GenerationLime.copy(alpha = 0.14f) else GenerationPanel)
            .border(1.dp, if (item.selected) GenerationLime.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.06f), RoundedCornerShape(15.dp))
            .clickable(onClick = item.onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(
                text = item.label,
                color = if (item.selected) GenerationLime else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (item.selected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(GenerationLime, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                }
            }
        }
        Text(
            text = item.hint,
            color = GenerationMuted,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GenerationControlsPanel(
    modifier: Modifier = Modifier,
    similarity: Float? = null,
    similarityLabel: String = "Image Similarity",
    onSimilarityChanged: ((Float) -> Unit)? = null,
    creativity: Float? = null,
    onCreativityChanged: ((Float) -> Unit)? = null,
    motion: Float? = null,
    onMotionChanged: ((Float) -> Unit)? = null,
    duration: Int? = null,
    onDurationChanged: ((Int) -> Unit)? = null,
    generations: Int,
    onGenerationsChanged: (Int) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GenerationPanel)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (similarity != null && onSimilarityChanged != null) {
            SliderBlock(similarityLabel, "${(similarity * 100).toInt()}%", similarity, onSimilarityChanged)
            GenerationDivider()
        }
        if (creativity != null && onCreativityChanged != null) {
            SliderBlock("Creativity", "${(creativity * 100).toInt()}%", creativity, onCreativityChanged)
            GenerationDivider()
        }
        if (motion != null && onMotionChanged != null) {
            SliderBlock("Motion", "${(motion * 100).toInt()}%", motion, onMotionChanged)
            GenerationDivider()
        }
        if (duration != null && onDurationChanged != null) {
            SliderBlock("Duration", "${duration}s", (duration - 5) / 10f, { onDurationChanged((5 + it * 10).toInt()) })
            GenerationDivider()
        }
        GenerationStepper(generations = generations, onGenerationsChanged = onGenerationsChanged)
    }
}

@Composable
private fun SliderBlock(label: String, valueText: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(valueText, color = GenerationLime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = GenerationLime,
                activeTrackColor = GenerationLime.copy(alpha = 0.58f),
                inactiveTrackColor = Color.White.copy(alpha = 0.13f)
            )
        )
    }
}

@Composable
private fun GenerationStepper(generations: Int, onGenerationsChanged: (Int) -> Unit) {
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
            Text("-", color = Color.White.copy(alpha = 0.72f), fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onGenerationsChanged(generations - 1) })
            Text("$generations", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("+", color = GenerationLime, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onGenerationsChanged(generations + 1) })
        }
    }
}

@Composable
private fun GenerationDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.06f))
    )
}

@Composable
fun GenerateNowButton(
    isGenerating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isGenerating,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GenerationLime,
            disabledContainerColor = GenerationLime.copy(alpha = 0.38f)
        )
    ) {
        if (isGenerating) {
            CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(21.dp))
            Spacer(modifier = Modifier.width(9.dp))
            Text("Generate Now", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun GenerationErrorText(error: String?, onDismissError: () -> Unit) {
    if (error != null) {
        Text(
            text = error,
            color = Color(0xFFFF7A7A),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onDismissError)
        )
    }
}
