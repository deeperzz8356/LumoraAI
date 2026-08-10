package com.deep.lumoraai.feature.createhub

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import com.deep.lumoraai.feature.createhub.model.VideoEngine
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.MediaViewerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import android.util.Base64
@Composable
fun CreateHubScreen(
    uiState: CreateHubUiState,
    initialPrompt: String? = null,
    initialTab: Int = 0,
    onGenerateImage: (String, String, Int, Int, String?, String?) -> Unit,
    onGenerateVideo: (String, String, String?, Int, String?, Int) -> Unit = { _, _, _, _, _, _ -> },
    onResetState: () -> Unit,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(initialTab) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "createhub",
                onSelected = onNavigate
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF0F1026), Color(0xFF070714))))
        ) {
            CreateHubContent(
                initialPrompt = initialPrompt,
                isGenerating = uiState is CreateHubUiState.Generating,
                onGenerateImage = onGenerateImage,
                onGenerateVideo = onGenerateVideo,
                onNext = onNext,
            )

            if (uiState is CreateHubUiState.ImageGenerated) {
                MediaViewerDialog(
                    filePath = uiState.filePath,
                    mediaType = "IMAGE",
                    mimeType = uiState.mimeType,
                    title = "Image Generated!",
                    onDismiss = onResetState,
                )
            }

            if (uiState is CreateHubUiState.VideoGenerated) {
                MediaViewerDialog(
                    filePath = uiState.filePath,
                    mediaType = "VIDEO",
                    mimeType = uiState.mimeType,
                    title = "Video Ready",
                    onDismiss = onResetState,
                )
            }

            if (uiState is CreateHubUiState.Error) {
                AlertDialog(
                    onDismissRequest = onResetState,
                    confirmButton = { Button(onClick = onResetState) { Text("OK") } },
                    title = { Text("Error") },
                    text = { Text(uiState.message) }
                )
            }
        }
    }
}

@Composable
private fun CreateHubContent(
    initialPrompt: String?,
    isGenerating: Boolean,
    onGenerateImage: (String, String, Int, Int, String?, String?) -> Unit,
    onGenerateVideo: (String, String, String?, Int, String?, Int) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf("Image") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CreateHubTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        if (selectedTab == "Image") {
            ImageFormContent(initialPrompt = initialPrompt, onGenerate = { prompt, style, w, h, neg, img -> onGenerateImage(prompt, style, w, h, neg, img) })
        } else {
            VideoFormContent(isGenerating = isGenerating, onGenerate = onGenerateVideo)
        }
    }
}

@Composable
private fun CreateHubTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color(0xFF161838), RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val tabs = listOf("Image", "Video")
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        color = if (isSelected) Color(0xFFCFBDFF) else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) Color(0xFF0F1026) else Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ImageFormContent(initialPrompt: String?, onGenerate: (String, String, Int, Int, String?, String?) -> Unit) {
    var prompt by remember(initialPrompt) { mutableStateOf(initialPrompt ?: "") }
    var negativePrompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cinematic") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var sourceImageB64 by remember { mutableStateOf<String?>(null) }
    var numOutputs by remember { mutableStateOf(1) }
    
    val baseCostPerImage = 1
    val totalCost = numOutputs * baseCostPerImage

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ImagePromptInput(prompt = prompt, onPromptChange = { prompt = it })
        ImageInspirationChips(onChipClick = { prompt = it })
        NegativePromptSelector(negativePrompt = negativePrompt, onNegativePromptChange = { negativePrompt = it })
        ImageTemplateSelector(sourceImageB64 = sourceImageB64, onSourceImageChange = { sourceImageB64 = it })
        Text("Configuration", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        CreativeStyleSelector(selectedStyle = selectedStyle, onStyleSelect = { selectedStyle = it })
        ImageAspectRatioSelector(selectedRatio = selectedRatio, onRatioSelect = { selectedRatio = it })
        OutputsCard(numOutputs = numOutputs, onOutputsChange = { numOutputs = it })
        ImageGenerateButton(
            costText = "$totalCost Credits",
            onClick = {
                val (w, h) = when (selectedRatio) {
                    "16:9" -> Pair(1024, 576)
                    "9:16" -> Pair(576, 1024)
                    else -> Pair(1024, 1024)
                }
                onGenerate(prompt, selectedStyle, w, h, negativePrompt, sourceImageB64) 
            }
        )
    }
}

@Composable
private fun ImagePromptInput(prompt: String, onPromptChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                placeholder = { Text("Describe the image you want to create...", color = Color.White.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
                Text("${prompt.length} / 1000", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ImageInspirationChips(onChipClick: (String) -> Unit) {
    val chips = listOf("Cyberpunk cityscape", "Cinematic portrait", "Studio lighting", "Hyper-realistic")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Inspiration", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.take(2).forEach { chip ->
                InspirationChip(text = chip, onClick = { onChipClick(chip) }, modifier = Modifier.weight(1f))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.drop(2).forEach { chip ->
                InspirationChip(text = chip, onClick = { onChipClick(chip) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InspirationChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun NegativePromptSelector(negativePrompt: String, onNegativePromptChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Negative Prompt", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(if (expanded) "▲" else "▼", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
        if (expanded) {
            OutlinedTextField(
                value = negativePrompt,
                onValueChange = onNegativePromptChange,
                placeholder = { Text("What to avoid (e.g., blurry, ugly, extra fingers)...", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color(0xFFA855F7).copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ImageTemplateSelector(sourceImageB64: String?, onSourceImageChange: (String?) -> Unit) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val convertBitmapToBase64 = { bitmap: Bitmap ->
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        onSourceImageChange(base64)
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
                convertBitmapToBase64(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            convertBitmapToBase64(bitmap)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Image Template (Img2Img)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161838))
            ) {
                Text(if (sourceImageB64 != null) "Change Image" else "Upload Image", color = Color.White)
            }
            Button(
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161838))
            ) {
                Text("Camera", color = Color.White)
            }
        }
        if (sourceImageB64 != null) {
            Text("Source image selected", color = Color(0xFFA855F7), fontSize = 12.sp)
        }
    }
}

@Composable
private fun CreativeStyleSelector(selectedStyle: String, onStyleSelect: (String) -> Unit) {
    val styles = listOf(
        Triple("Cinematic", R.drawable.style_anime, "Cinematic"),
        Triple("Digital Art", R.drawable.style_digital, "Digital Art"),
        Triple("Photographic", R.drawable.style_fantasy, "Photographic")
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Creative Style", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("View all", color = Color(0xFFA855F7), fontSize = 12.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            styles.forEach { (name, resId, key) ->
                StyleCardItem(
                    name = name,
                    resId = resId,
                    isSelected = selectedStyle == key,
                    onClick = { onStyleSelect(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StyleCardItem(
    name: String,
    resId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFFA855F7) else Color.Transparent
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
        )
        Text(name, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

@Composable
private fun ImageAspectRatioSelector(selectedRatio: String, onRatioSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Aspect Ratio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val items = listOf("1:1", "16:9", "9:16")
            items.forEach { ratio ->
                RatioSelectorCard(
                    label = ratio,
                    isSelected = selectedRatio == ratio,
                    onClick = { onRatioSelect(ratio) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RatioSelectorCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.05f)
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF161838))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val iconMod = when (label) {
                "1:1" -> Modifier.size(16.dp)
                "16:9" -> Modifier.size(22.dp, 12.dp)
                else -> Modifier.size(12.dp, 22.dp)
            }
            Box(modifier = iconMod.border(1.5.dp, Color.White, RoundedCornerShape(1.dp)))
            Text(label, color = Color.White, fontSize = 10.sp)
        }
    }
}

@Composable
private fun OutputsCard(numOutputs: Int, onOutputsChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Number of Outputs", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("—", color = Color.White, modifier = Modifier.clickable { 
                if (numOutputs > 1) onOutputsChange(numOutputs - 1)
            })
            Text(numOutputs.toString(), color = Color.White, fontWeight = FontWeight.Bold)
            Text("+", color = Color.White, modifier = Modifier.clickable { 
                if (numOutputs < 4) onOutputsChange(numOutputs + 1)
            })
        }
    }
}

@Composable
private fun ImageGenerateButton(costText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFBDFF))
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF0F1026), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate Image ($costText)", color = Color(0xFF0F1026), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Text("⋮", color = Color.White, fontSize = 20.sp)
        }
    }
}

@Composable
private fun VideoFormContent(
    isGenerating: Boolean,
    onGenerate: (String, String, String?, Int, String?, Int) -> Unit,
) {
    var prompt by remember { mutableStateOf("") }
    var selectedEngine by remember { mutableStateOf(VideoEngine.FAST_DRAFT) }
    var sourceImageB64 by remember { mutableStateOf<String?>(null) }
    var motionStrength by remember { mutableStateOf(65) }
    var cameraDirection by remember { mutableStateOf<String?>(null) }
    var duration by remember { mutableStateOf(10) }
    
    val videoCost = 5 // Base cost for video generation
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ImageTemplateSelector(sourceImageB64 = sourceImageB64, onSourceImageChange = { sourceImageB64 = it })
        VideoPromptInput(prompt = prompt, onPromptChange = { prompt = it })
        MotionDynamicsSelector(
            motionStrength = motionStrength, onMotionStrengthChange = { motionStrength = it },
            cameraDirection = cameraDirection, onCameraDirectionChange = { cameraDirection = it },
            duration = duration, onDurationChange = { duration = it }
        )
        GenerationEngineSelector(selectedEngine = selectedEngine, onEngineSelect = { selectedEngine = it })
        VideoGenerateButton(
            costText = "$videoCost Credits",
            isGenerating = isGenerating,
            onClick = { onGenerate(prompt, selectedEngine.modelId, sourceImageB64, motionStrength, cameraDirection, duration) }
        )
    }
}

// Reusing ImageTemplateSelector for source image upload

@Composable
private fun VideoPromptInput(prompt: String, onPromptChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("AI Vision Prompt", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            placeholder = { Text("Describe how the scene should come to life...", color = Color.White.copy(alpha = 0.3f)) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF161838),
                unfocusedContainerColor = Color(0xFF161838),
                focusedBorderColor = Color(0xFFA855F7).copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}

@Composable
private fun MotionDynamicsSelector(
    motionStrength: Int, onMotionStrengthChange: (Int) -> Unit,
    cameraDirection: String?, onCameraDirectionChange: (String?) -> Unit,
    duration: Int, onDurationChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Motion Dynamics", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        MotionStrengthRow(motionStrength, onMotionStrengthChange)
        CameraDirectionRow(cameraDirection, onCameraDirectionChange)
        DurationAndSfxRows(duration, onDurationChange)
    }
}

@Composable
private fun MotionStrengthRow(motionStrength: Int, onMotionStrengthChange: (Int) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Motion Strength", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Text(motionStrength.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        androidx.compose.material3.Slider(
            value = motionStrength.toFloat(),
            onValueChange = { onMotionStrengthChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = Color(0xFFA855F7),
                activeTrackColor = Color(0xFFA855F7),
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun CameraDirectionRow(cameraDirection: String?, onCameraDirectionChange: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Camera Direction", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val directions = listOf("ZOOM IN" to Icons.Default.Search, "PAN L" to Icons.Default.ArrowBack, "PAN R" to Icons.Default.ArrowForward, "AUTO" to Icons.Default.Refresh)
            directions.forEach { (name, icon) ->
                val isSelected = cameraDirection == name || (cameraDirection == null && name == "AUTO")
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(if (isSelected) Color(0xFFA855F7).copy(alpha = 0.2f) else Color(0xFF0F1026), RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable { onCameraDirectionChange(if (name == "AUTO") null else name) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, contentDescription = null, tint = if (isSelected) Color(0xFFA855F7) else Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(name, color = if (isSelected) Color(0xFFA855F7) else Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationAndSfxRows(duration: Int, onDurationChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clip Duration", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .clickable { onDurationChange(if (duration == 5) 10 else 5) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("$duration Seconds  ▼", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ambient SFX", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("●", color = Color(0xFFA855F7), fontSize = 20.sp)
        }
    }
}

@Composable
private fun GenerationEngineSelector(
    selectedEngine: VideoEngine,
    onEngineSelect: (VideoEngine) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Generation Engine", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EngineCard(
                name = VideoEngine.VEO_ULTRA.displayName,
                desc = "Cinematic Quality",
                isSelected = selectedEngine == VideoEngine.VEO_ULTRA,
                onClick = { onEngineSelect(VideoEngine.VEO_ULTRA) },
                modifier = Modifier.weight(1f)
            )
            EngineCard(
                name = VideoEngine.FAST_DRAFT.displayName,
                desc = "Lower Res",
                isSelected = selectedEngine == VideoEngine.FAST_DRAFT,
                onClick = { onEngineSelect(VideoEngine.FAST_DRAFT) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EngineCard(
    name: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.05f)
    Column(
        modifier = modifier
            .background(Color(0xFF161838), RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(desc, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
private fun VideoGenerateButton(costText: String, isGenerating: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isGenerating,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFBDFF))
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF0F1026),
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generating...", color = Color(0xFF0F1026), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        } else {
            Text("Generate with Veo ($costText)", color = Color(0xFF0F1026), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VideoPlayerDialog(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    MediaViewerDialog(
        filePath = videoUrl,
        mediaType = "VIDEO",
        onDismiss = onDismiss,
    )
}
