package com.deep.lumoraai.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.deep.lumoraai.core.components.CardVariant
import com.deep.lumoraai.core.components.PolishedTabScaffold
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
    PolishedTabScaffold(selectedRoute = "home", onNavigate = onNavigate, modifier = modifier) {
        when (uiState) {
            is HomeUiState.Loading -> AppLoadingScreen()
            is HomeUiState.Error -> AppErrorScreen(message = uiState.message)
            is HomeUiState.Empty -> AppEmptyScreen(title = "No Content", body = "Nothing to see here.")
            is HomeUiState.Success -> HomeContent(uiState = uiState, onNavigate = onNavigate)
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
            .padding(vertical = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        HomeTopBar(onNavigate = onNavigate)
        AppCard(variant = CardVariant.Elevated) {
            HomeOverviewSection(greeting = greeting, credits = uiState.credits, onNavigate = onNavigate)
        }
        HomeQuickActions(onNavigate = onNavigate)
        HomeRecentlyUsedSection()
        HomeToolsSection(title = "Production tools", icon = Icons.Default.Star, tools = imageTools + videoTools, onNavigate = onNavigate)
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
            Text("Lumora AI", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
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
private fun HomeOverviewSection(greeting: String, credits: Int, onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg), modifier = Modifier.padding(Spacing.lg)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(greeting, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "Manage generation work, review recent output, and jump back into the tools your team uses most.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        HomeSearchBar()
        HomeStatsGrid(credits = credits, onNavigate = onNavigate)
    }
}

@Composable
private fun HomeSearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search projects, templates, or tools", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
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
private fun HomeStatsGrid(credits: Int, onNavigate: (String) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 360.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            StatItem("CREATIONS", "48", modifier = Modifier.weight(if (compact) 1f else 1f))
            StatItem("PLAN", "Pro", modifier = Modifier.weight(if (compact) 1f else 1f))
        }
        Spacer(modifier = Modifier.height(Spacing.md))
        StatItem(
            label = "CREDITS",
            value = credits.toString(),
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigate(Screen.Credits.route) }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    val combinedModifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Column(
        modifier = combinedModifier
            .background(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), MaterialTheme.shapes.medium)
            .padding(Spacing.md)
    ) {
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
private fun HomeQuickActions(onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text("Quick actions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
            QuickActionCard(
                title = "Create image",
                subtitle = "Open the image job flow",
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.CreateHub.route) }
            )
            QuickActionCard(
                title = "Browse templates",
                subtitle = "Use a ready prompt",
                icon = Icons.Default.List,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Templates.route) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.fillMaxWidth()) {
            QuickActionCard(
                title = "Review queue",
                subtitle = "Check active jobs",
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Queue.route) }
            )
            QuickActionCard(
                title = "Billing",
                subtitle = "View credits and plan",
                icon = Icons.Default.Share,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Credits.route) }
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    AppCard(modifier = modifier, onClick = onClick, variant = CardVariant.Outlined) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
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
        RecentListItem(title = "Fantasy Portrait", time = "2 hours ago", resId = R.drawable.style_fantasy)
        RecentListItem(title = "Product Render", time = "Yesterday", resId = R.drawable.style_digital)
    }
}

@Composable
private fun RecentListItem(
    title: String,
    time: String,
    resId: Int,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth(), variant = CardVariant.Outlined) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
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
    AppCard(variant = CardVariant.Outlined) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text("Upgrade plan", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "Unlock 8K rendering, unlimited creations, and priority processing for active jobs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AppButton(
                text = "View plans",
                onClick = { onNavigate(Screen.Subscription.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
