package com.deep.lumoraai.feature.home

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
import androidx.compose.runtime.remember
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

data class ToolItemInfo(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val badge: String? = null
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(emptyList(), "home", {}) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF0F1026), Color(0xFF070714))))
        ) {
            HomeContent(onGenerate = onNext)
        }
    }
}

@Composable
private fun HomeContent(onGenerate: () -> Unit) {
    val imageTools = remember {
        listOf(
            ToolItemInfo("Text to Image", "Generate from prompt", "✨", "Popular"),
            ToolItemInfo("Templates", "Quick start styles", "🎴"),
            ToolItemInfo("Art Effects", "Style transfer", "🎨"),
            ToolItemInfo("AI Background", "Replace scenery", "🏞️", "New"),
            ToolItemInfo("Upscaler", "4K enhancement", "🔎", "Pro"),
            ToolItemInfo("BG Remover", "Instant transparent", "✂️")
        )
    }
    val videoTools = remember {
        listOf(
            ToolItemInfo("Image to Video", "Animate stills", "🎬"),
            ToolItemInfo("Text to Video", "Full scene gen", "🎥", "Pro"),
            ToolItemInfo("Video Templates", "Pre-made storyboards", "📋"),
            ToolItemInfo("Video Ads", "Convert any link", "📣", "New")
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HomeTopBar()
        HomeHeroSection()
        HomeRecentlyUsedSection()
        HomeToolsSection(title = "Image Tools", icon = "🖼️", tools = imageTools)
        HomeToolsSection(title = "Video Tools", icon = "📹", tools = videoTools)
        HomeUpgradeCard()
    }
}

@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⠿", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Lumora AI 4", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔔", fontSize = 20.sp, color = Color.White)
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.user_avatar),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape)
            )
        }
    }
}

@Composable
private fun HomeHeroSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Create", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Turn ideas into stunning images and videos with the next generation of AI creative tools.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        HomeSearchBar()
        HomeStatsRow()
    }
}

@Composable
private fun HomeSearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search tools, templates, or styles...", color = Color.White.copy(alpha = 0.3f)) },
        leadingIcon = { Text("🔍", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp)) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E214A).copy(alpha = 0.4f),
            unfocusedContainerColor = Color(0xFF1E214A).copy(alpha = 0.4f),
            focusedBorderColor = Color(0xFFA855F7).copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
private fun HomeStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem("CREATIONS", "48")
        StatItem("PLAN", "Pro")
        StatItem("CREDITS", "∞")
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeRecentlyUsedSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recently Used", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("View All", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecentItemCard(
                title = "Fantasy Portrait",
                time = "2 hours ago",
                resId = R.drawable.style_fantasy,
                modifier = Modifier.weight(1f)
            )
            RecentItemCard(
                title = "Product Render",
                time = "Yesterday",
                resId = R.drawable.style_digital,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecentItemCard(
    title: String,
    time: String,
    resId: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(time, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
private fun HomeToolsSection(
    title: String,
    icon: String,
    tools: List<ToolItemInfo>
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
            Text("$icon  $title", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        ToolsGrid(tools = tools)
    }
}

@Composable
private fun ToolsGrid(tools: List<ToolItemInfo>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in tools.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ToolCardItem(info = tools[i], modifier = Modifier.weight(1f))
                if (i + 1 < tools.size) {
                    ToolCardItem(info = tools[i + 1], modifier = Modifier.weight(1f))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ToolCardItem(
    info: ToolItemInfo,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(110.dp)
            .background(Color(0xFF0F1026), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable { /* Action */ }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(info.iconEmoji, fontSize = 20.sp)
            Column {
                Text(info.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(info.description, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, lineHeight = 12.sp)
            }
        }
        if (info.badge != null) {
            ToolBadge(text = info.badge, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun ToolBadge(text: String, modifier: Modifier = Modifier) {
    val bgColor = when (text) {
        "Popular" -> Color(0xFFCFBDFF).copy(alpha = 0.15f)
        "New" -> Color(0xFFADF021).copy(alpha = 0.15f)
        else -> Color(0xFFA855F7).copy(alpha = 0.15f)
    }
    val textColor = when (text) {
        "Popular" -> Color(0xFFCFBDFF)
        "New" -> Color(0xFFADF021)
        else -> Color(0xFFA855F7)
    }
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeUpgradeCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth().padding(vertical = 8.dp)
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Upgrade to Pro", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Unlock 8K rendering, unlimited creations, and priority queue processing for all your projects.",
                color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 16.sp
            )
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFBDFF))
            ) {
                Text("Get Started", color = Color(0xFF0F1026), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
