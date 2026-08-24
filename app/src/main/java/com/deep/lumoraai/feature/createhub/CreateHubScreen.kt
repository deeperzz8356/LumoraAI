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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.MediaViewerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Add
// PhotoLibrary replaced with Search (core icon)
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import android.util.Base64
import com.deep.lumoraai.core.theme.IntroPalette
import com.deep.lumoraai.core.theme.IntroTypography
import com.deep.lumoraai.core.components.LumoraIntroPrimaryButton
import com.deep.lumoraai.core.components.LumoraIntroSecondaryButton

import com.deep.lumoraai.core.components.LumoraIntroBackground
import com.deep.lumoraai.core.components.LumoraIntroLogo

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
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "createhub",
                onSelected = onNavigate
            )
        },
        containerColor = IntroPalette.BackgroundBase
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LumoraIntroBackground() // Adds the grid lines and stars/gradient from onboarding
            
            CreateHubContent(
                initialPrompt = initialPrompt,
                initialTab = initialTab,
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
                    title = { Text("Error", color = IntroPalette.TextPrimary) },
                    text = { Text(uiState.message, color = IntroPalette.TextPrimary) },
                    containerColor = IntroPalette.SurfaceRaised
                )
            }
        }
    }
}

@Composable
private fun CreateHubContent(
    initialPrompt: String?,
    initialTab: Int,
    isGenerating: Boolean,
    onGenerateImage: (String, String, Int, Int, String?, String?) -> Unit,
    onGenerateVideo: (String, String, String?, Int, String?, Int) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(if (initialTab == 0) "Image" else "Video") }
    
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 840.dp) // Adaptive max width
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CreateHubTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            if (selectedTab == "Image") {
                ImageFormContent(initialPrompt = initialPrompt, onGenerate = onGenerateImage)
            } else {
                VideoFormContent(isGenerating = isGenerating, onGenerate = onGenerateVideo)
            }
            Spacer(modifier = Modifier.height(24.dp))
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
            .padding(top = 16.dp, bottom = 8.dp)
            .background(IntroPalette.SurfaceRaised, RoundedCornerShape(24.dp))
            .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(24.dp))
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
                        color = if (isSelected) IntroPalette.AccentLime else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab,
                    color = if (isSelected) Color.Black else IntroPalette.TextMuted,
                    style = IntroTypography.buttonLabel
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
    
    val isCompact = LocalConfiguration.current.screenWidthDp < 600

    if (isCompact) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ImageConfigSection(
                negativePrompt, { negativePrompt = it },
                selectedStyle, { selectedStyle = it },
                selectedRatio, { selectedRatio = it },
                numOutputs, { numOutputs = it }
            )
            ImagePromptSection(prompt, { prompt = it }, { prompt = it }, sourceImageB64, { sourceImageB64 = it })
            ImageGenerateAction(selectedRatio, prompt, selectedStyle, negativePrompt, sourceImageB64, totalCost, onGenerate)
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    ImageConfigSection(
                        negativePrompt, { negativePrompt = it },
                        selectedStyle, { selectedStyle = it },
                        selectedRatio, { selectedRatio = it },
                        numOutputs, { numOutputs = it }
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    ImagePromptSection(prompt, { prompt = it }, { prompt = it }, sourceImageB64, { sourceImageB64 = it })
                }
            }
            ImageGenerateAction(selectedRatio, prompt, selectedStyle, negativePrompt, sourceImageB64, totalCost, onGenerate)
        }
    }
}

@Composable
private fun ImagePromptSection(prompt: String, onPromptChange: (String) -> Unit, onChipClick: (String) -> Unit, sourceImageB64: String?, onSourceImageChange: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ImagePromptInput(prompt = prompt, onPromptChange = onPromptChange, sourceImageB64 = sourceImageB64, onSourceImageChange = onSourceImageChange)
        ImageInspirationChips(onChipClick = onChipClick)
    }
}

@Composable
private fun ImageConfigSection(
    negativePrompt: String, onNegativePromptChange: (String) -> Unit,
    selectedStyle: String, onStyleSelect: (String) -> Unit,
    selectedRatio: String, onRatioSelect: (String) -> Unit,
    numOutputs: Int, onOutputsChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NegativePromptSelector(negativePrompt = negativePrompt, onNegativePromptChange = onNegativePromptChange)
        Text("Configuration", style = IntroTypography.sectionTitle, color = IntroPalette.TextPrimary)
        CreativeStyleSelector(selectedStyle = selectedStyle, onStyleSelect = onStyleSelect)
        ImageAspectRatioSelector(selectedRatio = selectedRatio, onRatioSelect = onRatioSelect)
        OutputsCard(numOutputs = numOutputs, onOutputsChange = onOutputsChange)
    }
}

@Composable
private fun ImageGenerateAction(
    selectedRatio: String, prompt: String, selectedStyle: String, negativePrompt: String,
    sourceImageB64: String?, totalCost: Int,
    onGenerate: (String, String, Int, Int, String?, String?) -> Unit
) {
    LumoraIntroPrimaryButton(
        text = "Generate Image ($totalCost Credits)",
        leadingIcon = Icons.Default.Star,
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

@Composable
private fun ImagePromptInput(prompt: String, onPromptChange: (String) -> Unit, sourceImageB64: String?, onSourceImageChange: (String?) -> Unit) {
    var showDropdown by remember { mutableStateOf(false) }
    var uploadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val convertBitmapToBase64 = { bitmap: Bitmap ->
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        uploadedBitmap = bitmap
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
        showDropdown = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            convertBitmapToBase64(bitmap)
        }
        showDropdown = false
    }

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(IntroPalette.SurfaceRaised, RoundedCornerShape(16.dp))
                .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    placeholder = { Text("Describe the image you want to create...", color = IntroPalette.TextMuted) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = IntroPalette.TextPrimary,
                        unfocusedTextColor = IntroPalette.TextPrimary
                    )
                )
                
                if (uploadedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            bitmap = uploadedBitmap!!.asImageBitmap(),
                            contentDescription = "Uploaded image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .clickable { 
                                    uploadedBitmap = null
                                    onSourceImageChange(null)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("×", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(IntroPalette.PrimaryButton, CircleShape)
                                .clickable { showDropdown = !showDropdown },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Icon(Icons.Default.Star, contentDescription = null, tint = IntroPalette.AccentLime, modifier = Modifier.size(20.dp))
                        Icon(Icons.Default.Search, contentDescription = null, tint = IntroPalette.TextMuted, modifier = Modifier.size(20.dp))
                    }
                    Text("${prompt.length} / 1000", color = IntroPalette.TextSubtle, fontSize = 11.sp)
                }
            }
        }
        
        if (showDropdown) {
            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = (-8).dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(8.dp)
                    .clickable { showDropdown = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { 
                                cameraLauncher.launch(null)
                                showDropdown = false 
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Camera", color = Color.White, fontSize = 14.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { 
                                galleryLauncher.launch("image/*")
                                showDropdown = false 
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Upload Image", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageInspirationChips(onChipClick: (String) -> Unit) {
    val chips = listOf("Cyberpunk cityscape", "Cinematic portrait", "Studio lighting", "Hyper-realistic")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = IntroPalette.AccentLime, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Inspiration", style = IntroTypography.buttonLabel, color = IntroPalette.TextMuted)
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
            .background(IntroPalette.SurfaceRaised, RoundedCornerShape(16.dp))
            .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = IntroPalette.TextMuted, style = IntroTypography.buttonLabel, maxLines = 1)
    }
}

@Composable
private fun NegativePromptSelector(negativePrompt: String, onNegativePromptChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntroPalette.SurfaceRaised, RoundedCornerShape(12.dp))
            .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(12.dp))
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
                    Icon(Icons.Default.Warning, contentDescription = null, tint = IntroPalette.AccentPink, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Negative Prompt", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
                }
                Text(if (expanded) "▲" else "▼", color = IntroPalette.TextMuted, fontSize = 12.sp)
            }
        }
        if (expanded) {
            OutlinedTextField(
                value = negativePrompt,
                onValueChange = onNegativePromptChange,
                placeholder = { Text("What to avoid (e.g., blurry, ugly)...", color = IntroPalette.TextMuted) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = IntroPalette.BackgroundBase,
                    unfocusedContainerColor = IntroPalette.BackgroundBase,
                    focusedBorderColor = IntroPalette.PrimaryButton.copy(alpha = 0.5f),
                    unfocusedBorderColor = IntroPalette.BorderSubtle,
                    focusedTextColor = IntroPalette.TextPrimary,
                    unfocusedTextColor = IntroPalette.TextPrimary
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
        Text("Image Template (Img2Img)", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LumoraIntroSecondaryButton(
                text = if (sourceImageB64 != null) "Change Image" else "Upload Image",
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f),
                height = 48.dp
            )
            LumoraIntroSecondaryButton(
                text = "Camera",
                onClick = { cameraLauncher.launch(null) },
                modifier = Modifier.weight(1f),
                height = 48.dp
            )
        }
        if (sourceImageB64 != null) {
            Text("Source image selected", color = IntroPalette.AccentLime, style = IntroTypography.body)
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
            Text("Creative Style", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
            Text("View all", color = IntroPalette.AccentLime, style = IntroTypography.body)
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
    val borderColor = if (isSelected) IntroPalette.PrimaryButton else Color.Transparent
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
        Text(name, color = if (isSelected) IntroPalette.TextPrimary else IntroPalette.TextMuted, style = IntroTypography.body)
    }
}

@Composable
private fun ImageAspectRatioSelector(selectedRatio: String, onRatioSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Aspect Ratio", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
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
    val borderColor = if (isSelected) IntroPalette.PrimaryButton else IntroPalette.BorderSubtle
    val bgColor = if (isSelected) IntroPalette.SurfaceRaised.copy(alpha = 0.5f) else IntroPalette.SurfaceRaised
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
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
            Box(modifier = iconMod.border(1.5.dp, if (isSelected) IntroPalette.AccentLime else IntroPalette.TextSubtle, RoundedCornerShape(1.dp)))
            Text(label, color = if (isSelected) IntroPalette.TextPrimary else IntroPalette.TextMuted, style = IntroTypography.body)
        }
    }
}

@Composable
private fun OutputsCard(numOutputs: Int, onOutputsChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntroPalette.SurfaceRaised, RoundedCornerShape(12.dp))
            .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Number of Outputs", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("—", color = IntroPalette.TextPrimary, modifier = Modifier.clickable { 
                if (numOutputs > 1) onOutputsChange(numOutputs - 1)
            })
            Text(numOutputs.toString(), style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
            Text("+", color = IntroPalette.TextPrimary, modifier = Modifier.clickable { 
                if (numOutputs < 4) onOutputsChange(numOutputs + 1)
            })
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
    var selectedVoiceType by remember { mutableStateOf("Natural") }
    var selectedAvatar by remember { mutableStateOf("None") }
    var selectedFormat by remember { mutableStateOf("Portrait") }
    var selectedDubbing by remember { mutableStateOf("None") }
    var duration by remember { mutableStateOf(10) }
    
    val videoCost = 5 // Base cost for video generation
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        VoiceTypeSelector(selectedVoiceType = selectedVoiceType, onVoiceTypeSelect = { selectedVoiceType = it })
        AvatarSelector(selectedAvatar = selectedAvatar, onAvatarSelect = { selectedAvatar = it })
        FormatSelector(selectedFormat = selectedFormat, onFormatSelect = { selectedFormat = it })
        DubbingSelector(selectedDubbing = selectedDubbing, onDubbingSelect = { selectedDubbing = it })
        TimingSelector(duration = duration, onDurationChange = { duration = it })
        GenerationEngineSelector(selectedEngine = selectedEngine, onEngineSelect = { selectedEngine = it })
        VideoPromptInput(prompt = prompt, onPromptChange = { prompt = it }, sourceImageB64 = sourceImageB64, onSourceImageChange = { sourceImageB64 = it })
        VideoGenerateButton(
            costText = "$videoCost Credits",
            isGenerating = isGenerating,
            onClick = { onGenerate(prompt, selectedEngine.modelId, sourceImageB64, 65, null, duration) }
        )
    }
}

@Composable
private fun VideoPromptInput(prompt: String, onPromptChange: (String) -> Unit, sourceImageB64: String?, onSourceImageChange: (String?) -> Unit) {
    var showDropdown by remember { mutableStateOf(false) }
    var uploadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val contentResolver = context.contentResolver

    val convertBitmapToBase64 = { bitmap: Bitmap ->
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        uploadedBitmap = bitmap
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
        showDropdown = false
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            convertBitmapToBase64(bitmap)
        }
        showDropdown = false
    }

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(IntroPalette.SurfaceRaised, RoundedCornerShape(16.dp))
                .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    placeholder = { Text("Describe how the scene should come to life...", color = IntroPalette.TextMuted) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = IntroPalette.TextPrimary,
                        unfocusedTextColor = IntroPalette.TextPrimary
                    )
                )
                
                if (uploadedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            bitmap = uploadedBitmap!!.asImageBitmap(),
                            contentDescription = "Uploaded image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .clickable { 
                                    uploadedBitmap = null
                                    onSourceImageChange(null)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("×", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(IntroPalette.PrimaryButton, CircleShape)
                                .clickable { showDropdown = !showDropdown },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Icon(Icons.Default.Star, contentDescription = null, tint = IntroPalette.AccentLime, modifier = Modifier.size(20.dp))
                        Icon(Icons.Default.Search, contentDescription = null, tint = IntroPalette.TextMuted, modifier = Modifier.size(20.dp))
                    }
                    Text("${prompt.length} / 1000", color = IntroPalette.TextSubtle, fontSize = 11.sp)
                }
            }
        }
        
        if (showDropdown) {
            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = (-8).dp)
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(8.dp)
                    .clickable { showDropdown = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { 
                                cameraLauncher.launch(null)
                                showDropdown = false 
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Camera", color = Color.White, fontSize = 14.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { 
                                galleryLauncher.launch("image/*")
                                showDropdown = false 
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Upload Image", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceTypeSelector(selectedVoiceType: String, onVoiceTypeSelect: (String) -> Unit) {
    val voiceTypes = listOf("Natural", "Professional", "Casual", "Energetic")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Voice Type", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            voiceTypes.forEach { voice ->
                val isSelected = selectedVoiceType == voice
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(if (isSelected) IntroPalette.PrimaryButton.copy(alpha = 0.2f) else IntroPalette.SurfaceRaised, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) IntroPalette.PrimaryButton else IntroPalette.BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onVoiceTypeSelect(voice) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(voice, color = if (isSelected) IntroPalette.PrimaryButton else IntroPalette.TextMuted, style = IntroTypography.body)
                }
            }
        }
    }
}

@Composable
private fun AvatarSelector(selectedAvatar: String, onAvatarSelect: (String) -> Unit) {
    val avatars = listOf(
        Triple("None", R.drawable.style_digital, "No Avatar"),
        Triple("Male", R.drawable.style_anime, "Male"),
        Triple("Female", R.drawable.style_fantasy, "Female"),
        Triple("Custom", R.drawable.style_digital, "Custom")
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Avatar", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            avatars.forEach { (name, resId, label) ->
                val isSelected = selectedAvatar == name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .height(70.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) IntroPalette.PrimaryButton else IntroPalette.BorderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                            .background(if (isSelected) IntroPalette.PrimaryButton.copy(alpha = 0.1f) else IntroPalette.SurfaceRaised)
                            .clickable { onAvatarSelect(name) }
                    ) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .background(IntroPalette.PrimaryButton, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        label,
                        color = if (isSelected) IntroPalette.PrimaryButton else IntroPalette.TextMuted,
                        style = IntroTypography.body,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatSelector(selectedFormat: String, onFormatSelect: (String) -> Unit) {
    val formats = listOf("Portrait", "Landscape", "Square", "Story")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Social Media Format", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            formats.forEach { format ->
                val isSelected = selectedFormat == format
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(if (isSelected) IntroPalette.PrimaryButton.copy(alpha = 0.2f) else IntroPalette.SurfaceRaised, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) IntroPalette.PrimaryButton else IntroPalette.BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onFormatSelect(format) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(format, color = if (isSelected) IntroPalette.PrimaryButton else IntroPalette.TextMuted, style = IntroTypography.body)
                }
            }
        }
    }
}

@Composable
private fun DubbingSelector(selectedDubbing: String, onDubbingSelect: (String) -> Unit) {
    val dubbingOptions = listOf("None", "English", "Spanish", "Hindi", "Auto")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Dubbing", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dubbingOptions.forEach { dubbing ->
                val isSelected = selectedDubbing == dubbing
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(if (isSelected) IntroPalette.PrimaryButton.copy(alpha = 0.2f) else IntroPalette.SurfaceRaised, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) IntroPalette.PrimaryButton else IntroPalette.BorderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onDubbingSelect(dubbing) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(dubbing, color = if (isSelected) IntroPalette.PrimaryButton else IntroPalette.TextMuted, style = IntroTypography.body, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun TimingSelector(duration: Int, onDurationChange: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntroPalette.SurfaceRaised, RoundedCornerShape(16.dp))
            .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = IntroPalette.TextPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Timing", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
        }
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Duration (seconds)", style = IntroTypography.body, color = IntroPalette.TextMuted)
                Text("${duration}s", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
            }
            androidx.compose.material3.Slider(
                value = duration.toFloat(),
                onValueChange = { onDurationChange(it.toInt()) },
                valueRange = 5f..60f,
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = IntroPalette.PrimaryButton,
                    activeTrackColor = IntroPalette.PrimaryButton,
                    inactiveTrackColor = IntroPalette.BorderSubtle
                )
            )
        }
    }
}

@Composable
private fun GenerationEngineSelector(
    selectedEngine: VideoEngine,
    onEngineSelect: (VideoEngine) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Generation Engine", style = IntroTypography.buttonLabel, color = IntroPalette.TextPrimary)
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
    val borderColor = if (isSelected) IntroPalette.PrimaryButton else IntroPalette.BorderSubtle
    Column(
        modifier = modifier
            .background(IntroPalette.SurfaceRaised, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = IntroPalette.AccentLime, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(name, color = IntroPalette.TextPrimary, style = IntroTypography.buttonLabel)
        }
        Text(desc, color = IntroPalette.TextMuted, style = IntroTypography.body)
    }
}

@Composable
private fun VideoGenerateButton(costText: String, isGenerating: Boolean, onClick: () -> Unit) {
    LumoraIntroPrimaryButton(
        text = if (isGenerating) "Generating..." else "Generate with Veo ($costText)",
        leadingContent = if (isGenerating) {
            { CircularProgressIndicator(modifier = Modifier.size(16.dp), color = IntroPalette.TextPrimary, strokeWidth = 2.dp) }
        } else null,
        onClick = onClick,
        enabled = !isGenerating
    )
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
