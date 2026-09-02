package com.deep.lumoraai.feature.generation

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.deep.lumoraai.core.components.LocalVideoPlayer
import com.deep.lumoraai.core.utils.MediaGallerySaver
import com.deep.lumoraai.core.utils.MediaShareUtils
import com.deep.lumoraai.feature.imagetoimage.ImageStyle
import com.deep.lumoraai.feature.imagetoimage.VideoStyle
import kotlinx.coroutines.launch
import java.io.File

val GenerationScreenBg = Color(0xFF081020)
val GenerationPanel = Color(0xFF151D31)
val GenerationLime = Color(0xFFD6FF2F)
val GenerationMuted = Color(0xFF9AA5B8)

enum class GenerationAspectRatio(
    val label: String,
    val description: String,
    val width: Int,
    val height: Int,
    val promptHint: String,
) {
    Portrait("2:3", "Portrait", 1024, 1536, "vertical 2:3 portrait composition"),
    Story("9:16", "Story", 1080, 1920, "vertical 9:16 story composition"),
    Square("1:1", "Square", 1024, 1024, "centered 1:1 square composition"),
    Landscape("16:9", "Wide", 1536, 864, "wide 16:9 landscape composition");

    val displayLabel: String = "$label ${description.lowercase()}"
}

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
    isSettingsOpen: Boolean = false,
    onSettingsClick: () -> Unit = {},
) {
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
                contentDescription = "Advanced settings",
                onClick = onSettingsClick,
                highlighted = isSettingsOpen || negativePrompt.isNotBlank()
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
fun ImageStyleSection(
    selected: ImageStyle,
    onSelected: (ImageStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    StyleSection(
        title = "Style",
        items = ImageStyle.entries.map { StyleItem(it.label, it.promptHint, it.assetFileName, selected == it) { onSelected(it) } },
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
        items = VideoStyle.entries.map { StyleItem(it.label, it.promptHint, it.assetFileName, selected == it) { onSelected(it) } },
        modifier = modifier
    )
}

private data class StyleItem(
    val label: String,
    val hint: String,
    val assetFileName: String,
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
    Box(
        modifier = Modifier
            .width(142.dp)
            .height(176.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (item.selected) GenerationLime.copy(alpha = 0.14f) else GenerationPanel)
            .border(1.dp, if (item.selected) GenerationLime.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.06f), RoundedCornerShape(15.dp))
            .clickable(onClick = item.onClick)
    ) {
        AsyncImage(
            model = assetUri(item.assetFileName),
            contentDescription = item.label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(142.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f))
        )
        if (item.selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .background(GenerationLime, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color(0xDD101827))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.label,
                color = if (item.selected) GenerationLime else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.hint,
                color = GenerationMuted,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun assetUri(fileName: String): String = "file:///android_asset/${Uri.encode(fileName)}"

@Composable
fun GenerationControlsPanel(
    modifier: Modifier = Modifier,
    mediaType: String,
    selectedAspectRatio: GenerationAspectRatio,
    onAspectRatioSelected: (GenerationAspectRatio) -> Unit,
    negativePrompt: String,
    onNegativePromptChanged: (String) -> Unit,
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Advanced Settings", color = Color.White, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("Tune output details before choosing a style.", color = GenerationMuted, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Text(
                text = mediaType.uppercase(),
                color = Color.Black,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(GenerationLime)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        NegativePromptField(value = negativePrompt, onValueChange = onNegativePromptChanged)
        GenerationDivider()
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
    }
}

@Composable
fun GenerationAspectRatioSection(
    selected: GenerationAspectRatio,
    onSelected: (GenerationAspectRatio) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Ratio", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(selected.displayLabel, color = GenerationLime, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenerationAspectRatio.entries.forEach { ratio ->
                RatioChip(
                    ratio = ratio,
                    selected = ratio == selected,
                    onClick = { onSelected(ratio) }
                )
            }
        }
    }
}

@Composable
private fun RatioChip(ratio: GenerationAspectRatio, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(86.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) GenerationLime.copy(alpha = 0.15f) else Color(0xFF10182A))
            .border(1.dp, if (selected) GenerationLime.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(ratio.label, color = if (selected) GenerationLime else Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        Text(ratio.description, color = GenerationMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NegativePromptField(value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Negative Prompt", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${value.length}/1000", color = Color.White.copy(alpha = 0.56f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.take(1000)) },
            placeholder = { Text("blurry, extra fingers, bad anatomy, watermark...") },
            minLines = 3,
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
fun GenerationCountSection(generations: Int, onGenerationsChanged: (Int) -> Unit) {
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
    enabled: Boolean = true,
    creditCost: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isGenerating,
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
            Text(
                text = creditCost?.let { "Generate Now (-$it Credits)" } ?: "Generate Now",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GeneratedMediaLoading(
    isVisible: Boolean,
    mediaType: String,
    modifier: Modifier = Modifier,
) {
    if (!isVisible) return

    val isVideo = mediaType.equals("VIDEO", ignoreCase = true)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GenerationPanel)
            .border(1.dp, GenerationLime.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GenerationLime.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = GenerationLime,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(25.dp)
                )
                Icon(
                    imageVector = if (isVideo) Icons.Default.AutoAwesome else Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isVideo) "Video is generating" else "Image is generating",
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Rendering in the queue. Your result will load here when ready.",
                    color = GenerationMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            color = GenerationLime,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun GeneratedMediaResult(
    filePath: String?,
    mediaType: String,
    mimeType: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filePath == null) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showViewer by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    val file = remember(filePath) { File(filePath) }
    val exists = file.exists()
    val isVideo = mediaType.equals("VIDEO", ignoreCase = true) || mimeType.startsWith("video/", ignoreCase = true)
    val writePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch {
                val result = MediaGallerySaver.saveToGallery(context, filePath, mimeType, mediaType)
                Toast.makeText(context, result.getOrElse { it.message ?: "Could not download media." }, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Storage permission is needed to download this file.", Toast.LENGTH_SHORT).show()
        }
    }

    if (showViewer) {
        GeneratedMediaViewer(
            filePath = filePath,
            mediaType = mediaType,
            mimeType = mimeType,
            onDismiss = { showViewer = false },
            onEdit = {
                showViewer = false
                onEdit()
            }
        )
    }

    if (showFeedback) {
        FeedbackDialog(
            onDismiss = { showFeedback = false },
            onSubmit = { reason ->
                saveGeneratedFeedback(context, filePath, mediaType, reason)
                showFeedback = false
                Toast.makeText(context, "Thanks, feedback saved.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GenerationPanel)
            .border(1.dp, GenerationLime.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isVideo) "Video Ready" else "Image Ready",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Saved below. Tap to open viewer.",
                    color = GenerationMuted,
                    fontSize = 12.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isVideo) 260.dp else 320.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .clickable(enabled = exists) { showViewer = true },
            contentAlignment = Alignment.Center
        ) {
            if (!exists) {
                Text("Saved media file is missing.", color = Color.White, modifier = Modifier.padding(16.dp))
            } else if (isVideo) {
                LocalVideoPlayer(filePath = filePath)
            } else {
                AsyncImage(
                    model = file,
                    contentDescription = "Generated image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            if (exists) {
                ResultActionIcon(
                    icon = Icons.Default.Fullscreen,
                    contentDescription = "Open viewer",
                    onClick = { showViewer = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ResultActionIcon(Icons.Default.Edit, "Edit", onClick = onEdit)
            ResultActionIcon(Icons.Default.ThumbUp, "Like") {
                saveGeneratedFeedback(context, filePath, mediaType, "Like")
                Toast.makeText(context, "Glad you liked it.", Toast.LENGTH_SHORT).show()
            }
            ResultActionIcon(Icons.Default.ThumbDown, "Dislike") {
                saveGeneratedFeedback(context, filePath, mediaType, "Dislike")
                Toast.makeText(context, "Noted.", Toast.LENGTH_SHORT).show()
            }
            ResultActionIcon(Icons.Default.Feedback, "Feedback") {
                showFeedback = true
            }
            ResultActionIcon(Icons.Default.Share, "Share", enabled = exists) {
                MediaShareUtils.shareMedia(context, filePath, mimeType)
            }
            ResultActionIcon(Icons.Default.Download, "Download", enabled = exists) {
                if (MediaGallerySaver.hasWritePermission(context)) {
                    scope.launch {
                        val result = MediaGallerySaver.saveToGallery(context, filePath, mimeType, mediaType)
                        Toast.makeText(context, result.getOrElse { it.message ?: "Could not download media." }, Toast.LENGTH_SHORT).show()
                    }
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }
}

@Composable
private fun GeneratedMediaViewer(
    filePath: String,
    mediaType: String,
    mimeType: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    val context = LocalContext.current
    val isVideo = mediaType.equals("VIDEO", ignoreCase = true) || mimeType.startsWith("video/", ignoreCase = true)
    val file = remember(filePath) { File(filePath) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF050914))
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!file.exists()) {
                    Text("Saved media file is missing.", color = Color.White)
                } else if (isVideo) {
                    LocalVideoPlayer(filePath = filePath)
                } else {
                    AsyncImage(
                        model = file,
                        contentDescription = "Generated image viewer",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultActionIcon(Icons.Default.Share, "Share") {
                    MediaShareUtils.shareMedia(context, filePath, mimeType)
                }
                ResultActionIcon(Icons.Default.Edit, "Edit", onClick = onEdit)
                ResultActionIcon(Icons.Default.Close, "Close", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun ResultActionIcon(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = if (enabled) 0.58f else 0.26f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = if (enabled) Color.White else Color.White.copy(alpha = 0.35f), modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun FeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    val options = listOf("Prompt mismatch", "Low quality", "Composition issue", "Style issue", "Motion issue", "Other")
    var selected by remember { mutableStateOf(setOf<String>()) }
    var draft by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GenerationPanel,
        title = { Text("Feedback", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("What should be better in this result?", color = GenerationMuted, fontSize = 13.sp)
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selected = if (option in selected) selected - option else selected + option
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = option in selected,
                            onCheckedChange = { checked ->
                                selected = if (checked) selected + option else selected - option
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = GenerationLime,
                                uncheckedColor = GenerationMuted,
                                checkmarkColor = Color.Black
                            )
                        )
                        Text(option, color = Color.White, fontSize = 13.sp)
                    }
                }
                if ("Other" in selected) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(400) },
                        minLines = 3,
                        placeholder = { Text("Tell us what felt off...") },
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
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val reason = buildString {
                        append(selected.ifEmpty { setOf("General feedback") }.joinToString())
                        if ("Other" in selected && draft.isNotBlank()) append(": $draft")
                    }
                    onSubmit(reason)
                }
            ) {
                Text("Send", color = GenerationLime, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GenerationMuted)
            }
        }
    )
}

private fun saveGeneratedFeedback(context: android.content.Context, filePath: String, mediaType: String, reason: String) {
    val prefs = context.applicationContext.getSharedPreferences("generated_media_feedback", android.content.Context.MODE_PRIVATE)
    val existing = prefs.getStringSet("records", emptySet()).orEmpty()
    val record = listOf(System.currentTimeMillis().toString(), mediaType, reason, filePath).joinToString("|")
    prefs.edit().putStringSet("records", existing + record).apply()
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
