package com.deep.lumoraai.feature.createhub

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.BottomNavigationBar

@Composable
fun CreateHubScreen(
    uiState: CreateHubUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "createhub",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF0F1026), Color(0xFF070714))))
        ) {
            CreateHubContent(onNext = onNext)
        }
    }
}

@Composable
private fun CreateHubContent(onNext: () -> Unit, modifier: Modifier = Modifier) {
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
            ImageFormContent(onGenerate = onNext)
        } else {
            VideoFormContent(onGenerate = onNext)
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
private fun ImageFormContent(onGenerate: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cinematic") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ImagePromptInput(prompt = prompt, onPromptChange = { prompt = it })
        ImageInspirationChips(onChipClick = { prompt = it })
        NegativePromptSelector()
        Text("Configuration", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        CreativeStyleSelector(selectedStyle = selectedStyle, onStyleSelect = { selectedStyle = it })
        ImageAspectRatioSelector(selectedRatio = selectedRatio, onRatioSelect = { selectedRatio = it })
        OutputsCard()
        PublicToggleCard()
        ImageGenerateButton(onClick = onGenerate)
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
                    Text("🪄", fontSize = 18.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("🖼️", fontSize = 18.sp, color = Color.White.copy(alpha = 0.6f))
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
            Text("📍", fontSize = 12.sp)
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
private fun NegativePromptSelector() {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⛔", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Negative Prompt", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(if (expanded) "▲" else "▼", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
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
private fun OutputsCard() {
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
            Text("—", color = Color.White, modifier = Modifier.clickable {})
            Text("1", color = Color.White, fontWeight = FontWeight.Bold)
            Text("+", color = Color.White, modifier = Modifier.clickable {})
        }
    }
}

@Composable
private fun PublicToggleCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌐", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Public Generation", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Visible in the community showcase", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
            }
        }
        Text("●", color = Color(0xFFA855F7), fontSize = 24.sp)
    }
}

@Composable
private fun ImageGenerateButton(onClick: () -> Unit) {
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
            Text("✨  Generate Image", color = Color(0xFF0F1026), fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
private fun VideoFormContent(onGenerate: () -> Unit) {
    var prompt by remember { mutableStateOf("") }
    var selectedEngine by remember { mutableStateOf("Veo-1 Ultra") }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        VideoSourceSelector()
        VideoPromptInput(prompt = prompt, onPromptChange = { prompt = it })
        MotionDynamicsSelector()
        GenerationEngineSelector(selectedEngine = selectedEngine, onEngineSelect = { selectedEngine = it })
        VideoGenerateButton(onClick = onGenerate)
    }
}

@Composable
private fun VideoSourceSelector() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.style_fantasy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("➕", fontSize = 24.sp, color = Color.White)
                Text("Click to replace source image", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(32.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) { Text("🔍", fontSize = 12.sp) }
            Box(modifier = Modifier.size(32.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) { Text("🖼️", fontSize = 12.sp) }
        }
        Text("16:9 CINEMA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp))
    }
}

@Composable
private fun VideoPromptInput(prompt: String, onPromptChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("AI Vision Prompt", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            placeholder = { Text("Describe how the scene should come to life... (e.g. 'Slow cinematic zoom into the neon lights with rain falling softly')", color = Color.White.copy(alpha = 0.3f)) },
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
private fun MotionDynamicsSelector() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎬  Motion Dynamics", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        MotionStrengthRow()
        CameraDirectionRow()
        DurationAndSfxRows()
    }
}

@Composable
private fun MotionStrengthRow() {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Motion Strength", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Text("65", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.65f).fillMaxHeight().background(Color(0xFFA855F7), CircleShape))
        }
    }
}

@Composable
private fun CameraDirectionRow() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Camera Direction", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val directions = listOf("ZOOM IN" to "🔍", "PAN L" to "👈", "PAN R" to "👉", "AUTO" to "🔄")
            directions.forEach { (name, icon) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(Color(0xFF0F1026), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(icon, fontSize = 10.sp)
                        Text(name, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationAndSfxRows() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏱️", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clip Duration", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("10 Seconds  ▼", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎵", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ambient SFX", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Text("●", color = Color(0xFFA855F7), fontSize = 20.sp)
        }
    }
}

@Composable
private fun GenerationEngineSelector(selectedEngine: String, onEngineSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("⚙️  Generation Engine", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EngineCard(
                name = "Veo-1 Ultra",
                desc = "Cinematic Quality",
                isSelected = selectedEngine == "Veo-1 Ultra",
                onClick = { onEngineSelect("Veo-1 Ultra") },
                modifier = Modifier.weight(1f)
            )
            EngineCard(
                name = "FastDraft",
                desc = "Lower Res",
                isSelected = selectedEngine == "FastDraft",
                onClick = { onEngineSelect("FastDraft") },
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
            Text(if (name == "Veo-1 Ultra") "⭐" else "⚡", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(desc, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
private fun VideoGenerateButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFBDFF))
    ) {
        Text("Generate with Veo", color = Color(0xFF0F1026), fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}