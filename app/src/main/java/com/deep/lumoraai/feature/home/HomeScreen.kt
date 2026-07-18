package com.deep.lumoraai.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppButton
import com.deep.lumoraai.core.components.AppCard
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.ui.theme.tokens.Spacing

data class ToolItemInfo(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String? = null
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(emptyList(), "home", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is HomeUiState.Loading -> AppLoadingScreen()
                is HomeUiState.Error -> AppErrorScreen(message = uiState.message)
                is HomeUiState.Empty -> AppEmptyScreen(title = "No Content", body = "Nothing to see here.")
                is HomeUiState.Success -> HomeContent(uiState = uiState, onNavigate = onNavigate)
            }
        }
    }
}

@Composable
private fun HomeContent(uiState: HomeUiState.Success, onNavigate: (String) -> Unit) {
    val imageTools = remember {
        listOf(
            ToolItemInfo("Text to Image", "Generate from prompt", Icons.Default.Star, "Popular"),
            ToolItemInfo("Templates", "Quick start styles", Icons.Default.List),
            ToolItemInfo("Art Effects", "Style transfer", Icons.Default.Edit),
            ToolItemInfo("AI Background", "Replace scenery", Icons.Default.Share, "New"),
            ToolItemInfo("Upscaler", "4K enhancement", Icons.Default.Search, "Pro"),
            ToolItemInfo("BG Remover", "Instant transparent", Icons.Default.Close)
        )
    }
    val videoTools = remember {
        listOf(
            ToolItemInfo("Image to Video", "Animate stills", Icons.Default.PlayArrow),
            ToolItemInfo("Text to Video", "Full scene gen", Icons.Default.PlayArrow, "Pro"),
            ToolItemInfo("Video Templates", "Pre-made storyboards", Icons.Default.List),
            ToolItemInfo("Video Ads", "Convert any link", Icons.Default.Share, "New")
        )
    }
    
    val greeting = if (uiState.items.isNotEmpty()) uiState.items.first() else "Create"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.containerMargin, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        HomeTopBar(onNavigate = onNavigate)
        HomeHeroSection(greeting = greeting, credits = uiState.credits, onNavigate = onNavigate)
        HomeRecentlyUsedSection()
        HomeToolsSection(title = "Image Tools", icon = Icons.Default.Star, tools = imageTools, onNavigate = onNavigate)
        HomeToolsSection(title = "Video Tools", icon = Icons.Default.PlayArrow, tools = videoTools, onNavigate = onNavigate)
        HomeUpgradeCard(onNavigate = onNavigate)
    }
}

@Composable
private fun HomeTopBar(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(Spacing.md))
            Text("Lumora AI 4", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onNavigate(Screen.Notifications.route) }
            )
            Spacer(modifier = Modifier.width(Spacing.lg))
            Image(
                painter = painterResource(id = R.drawable.user_avatar),
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
            )
        }
    }
}

@Composable
private fun HomeHeroSection(greeting: String, credits: Int, onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(greeting, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = "Turn ideas into stunning images and videos with the next generation of AI creative tools.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HomeSearchBar()
        HomeStatsRow(credits = credits, onNavigate = onNavigate)
    }
}

@Composable
private fun HomeSearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search tools, templates, or styles...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        singleLine = true
    )
}

@Composable
private fun HomeStatsRow(credits: Int, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem("CREATIONS", "48")
        StatItem("PLAN", "Pro")
        StatItem("CREDITS", credits.toString(), onClick = { onNavigate(Screen.Credits.route) })
    }
}

@Composable
private fun StatItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    val modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(modifier = modifier) {
        Text(
            label, 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun HomeRecentlyUsedSection() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recently Used", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("View All", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
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
                .aspectRatio(1.5f)
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
        Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HomeToolsSection(
    title: String,
    icon: ImageVector,
    tools: List<ToolItemInfo>,
    onNavigate: (String) -> Unit
) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        ToolsGrid(tools = tools, onNavigate = onNavigate)
    }
}

@Composable
private fun ToolsGrid(tools: List<ToolItemInfo>, onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        for (i in tools.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                ToolCardItem(info = tools[i], onNavigate = onNavigate, modifier = Modifier.weight(1f))
                if (i + 1 < tools.size) {
                    ToolCardItem(info = tools[i + 1], onNavigate = onNavigate, modifier = Modifier.weight(1f))
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
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = {
            when (info.title) {
                "Text to Image" -> onNavigate(Screen.CreateHub.route)
                "Templates" -> onNavigate(Screen.Templates.route)
                "Image to Video" -> onNavigate(Screen.CreateHub.route + "?tab=1")
                "Text to Video" -> onNavigate(Screen.CreateHub.route + "?tab=1")
                else -> {}
            }
        },
        modifier = modifier.height(110.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.padding(Spacing.md)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(info.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                Column {
                    Text(info.title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(info.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (info.badge != null) {
                ToolBadge(text = info.badge, modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

@Composable
private fun ToolBadge(text: String, modifier: Modifier = Modifier) {
    val bgColor = when (text) {
        "Popular" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        "New" -> Color(0xFFADF021).copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
    }
    val textColor = when (text) {
        "Popular" -> MaterialTheme.colorScheme.primary
        "New" -> Color(0xFFADF021)
        else -> MaterialTheme.colorScheme.secondary
    }
    Box(
        modifier = modifier
            .background(bgColor, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = Spacing.sm, vertical = 2.dp)
    ) {
        Text(text = text, color = textColor, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun HomeUpgradeCard(onNavigate: (String) -> Unit) {
    AppCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text("Upgrade to Pro", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "Unlock 8K rendering, unlimited creations, and priority queue processing for all your projects.",
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            AppButton(
                text = "Get Started",
                onClick = { onNavigate(Screen.Subscription.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
