package com.deep.lumoraai.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.LumoraIntroBackground
import com.deep.lumoraai.core.components.LumoraIntroPrimaryButton
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.createHubRoute
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.theme.IntroPalette
import com.deep.lumoraai.core.theme.IntroTypography
import kotlinx.coroutines.launch

private val SectionShape = RoundedCornerShape(20.dp)
private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = IntroPalette.BackgroundBase,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavigationBar(emptyList(), "home", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LumoraIntroBackground()
            when (uiState) {
                is HomeUiState.Loading -> AppLoadingScreen()
                is HomeUiState.Error -> AppErrorScreen(message = uiState.message)
                is HomeUiState.Empty -> AppEmptyScreen(title = "No Content", body = "Nothing to see here.")
                is HomeUiState.Success -> HomeContent(
                    uiState = uiState,
                    onNavigate = onNavigate,
                    onComingSoon = {
                        scope.launch { snackbarHostState.showSnackbar("Coming soon") }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState.Success,
    onNavigate: (String) -> Unit,
    onComingSoon: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HomeTopBar(
            userName = uiState.userName,
            credits = uiState.credits,
            onNavigate = onNavigate
        )
        HomeStatsRow(
            creationCount = uiState.creationCount,
            planLabel = uiState.planLabel,
            credits = uiState.credits,
            onNavigate = onNavigate
        )
        HomeTemplatesSection(onNavigate = onNavigate)
        if (uiState.recentItems.isNotEmpty()) {
            HomeRecentlyUsedSection(items = uiState.recentItems)
        }
        HomeToolsSection(
            title = "Image Tools",
            icon = Icons.Default.Star,
            tools = homeImageTools,
            onNavigate = onNavigate,
            onComingSoon = onComingSoon
        )
        HomeToolsSection(
            title = "Video Tools",
            icon = Icons.Default.PlayArrow,
            tools = homeVideoTools,
            onNavigate = onNavigate,
            onComingSoon = onComingSoon
        )
        HomeUpgradeCard(onNavigate = onNavigate)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HomeTopBar(
    userName: String,
    credits: Int,
    onNavigate: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Hello,", style = IntroTypography.greetingLabel)
            Text(
                text = userName,
                style = IntroTypography.greetingName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CreditsChip(
                credits = credits,
                onClick = { onNavigate(Screen.Credits.route) }
            )
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = IntroPalette.TextPrimary,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onNavigate(Screen.Notifications.route) }
            )
            Image(
                painter = painterResource(id = com.deep.lumoraai.R.drawable.user_avatar),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigate(Screen.Profile.route) }
            )
        }
    }
}

@Composable
private fun CreditsChip(credits: Int, onClick: () -> Unit) {
    val label = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        "$credits credits"
    }
    Box(
        modifier = Modifier
            .background(IntroPalette.SurfaceRaised, RoundedCornerShape(20.dp))
            .border(1.dp, IntroPalette.BorderSubtle, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, style = IntroTypography.creditsChip)
    }
}

@Composable
private fun HomeStatsRow(
    creationCount: Int,
    planLabel: String,
    credits: Int,
    onNavigate: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntroPalette.SurfaceRaised, SectionShape)
            .border(1.dp, IntroPalette.BorderSubtle, SectionShape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(label = "CREATIONS", value = creationCount.toString())
        StatItem(label = "PLAN", value = planLabel)
        StatItem(
            label = "CREDITS",
            value = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) "Unlimited" else credits.toString(),
            onClick = { onNavigate(Screen.Credits.route) }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    val modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = IntroTypography.statLabel)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = IntroTypography.statValue)
    }
}

@Composable
private fun HomeTemplatesSection(onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Templates", style = IntroTypography.sectionTitle)
            Text(
                text = "More",
                style = IntroTypography.sectionLink,
                modifier = Modifier.clickable { onNavigate(Screen.Templates.route) }
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(homeTemplatePreviews, key = { it.id }) { template ->
                HomeTemplateCard(
                    template = template,
                    onClick = { onNavigate(createHubRoute(prompt = template.prompt)) }
                )
            }
        }
    }
}

@Composable
private fun HomeTemplateCard(template: HomeTemplatePreview, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(id = template.imageRes),
            contentDescription = template.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(CardShape)
                .border(1.dp, IntroPalette.BorderSubtle, CardShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = template.title,
            style = IntroTypography.cardTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeRecentlyUsedSection(items: List<HomeRecentItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Recently Used", style = IntroTypography.sectionTitle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { item ->
                RecentItemCard(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecentItemCard(item: HomeRecentItem, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(CardShape)
                .border(1.dp, IntroPalette.BorderSubtle, CardShape)
        ) {
            if (!item.mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.mediaUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = item.fallbackImageRes),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = IntroTypography.cardTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(text = item.timeLabel, style = IntroTypography.cardSubtitle)
    }
}

@Composable
private fun HomeToolsSection(
    title: String,
    icon: ImageVector,
    tools: List<HomeToolItem>,
    onNavigate: (String) -> Unit,
    onComingSoon: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntroPalette.SurfaceRaised, SectionShape)
            .border(1.dp, IntroPalette.BorderSubtle, SectionShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = IntroPalette.SecondaryText, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = IntroTypography.sectionTitle)
        }
        ToolsGrid(tools = tools, onNavigate = onNavigate, onComingSoon = onComingSoon)
    }
}

@Composable
private fun ToolsGrid(
    tools: List<HomeToolItem>,
    onNavigate: (String) -> Unit,
    onComingSoon: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (i in tools.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToolCardItem(
                    info = tools[i],
                    onNavigate = onNavigate,
                    onComingSoon = onComingSoon,
                    modifier = Modifier.weight(1f)
                )
                if (i + 1 < tools.size) {
                    ToolCardItem(
                        info = tools[i + 1],
                        onNavigate = onNavigate,
                        onComingSoon = onComingSoon,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ToolCardItem(
    info: HomeToolItem,
    onNavigate: (String) -> Unit,
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = { handleToolClick(info, onNavigate, onComingSoon) },
        modifier = modifier.height(108.dp),
        shape = CardShape,
        color = IntroPalette.BackgroundBase,
        border = BorderStroke(1.dp, IntroPalette.BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    info.icon,
                    contentDescription = null,
                    tint = IntroPalette.TextMuted,
                    modifier = Modifier.size(20.dp)
                )
                if (info.badge != null) {
                    ToolBadge(text = info.badge)
                }
            }
            Column {
                Text(
                    info.title,
                    style = IntroTypography.toolTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    info.description,
                    style = IntroTypography.toolDescription,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun handleToolClick(
    info: HomeToolItem,
    onNavigate: (String) -> Unit,
    onComingSoon: () -> Unit,
) {
    when (info.destination) {
        HomeToolDestination.CreateHubImage -> onNavigate(createHubRoute(tab = 0))
        HomeToolDestination.CreateHubVideo -> onNavigate(createHubRoute(tab = 1))
        HomeToolDestination.Templates -> onNavigate(Screen.Templates.route)
        HomeToolDestination.ComingSoon -> onComingSoon()
    }
}

@Composable
private fun ToolBadge(text: String, modifier: Modifier = Modifier) {
    val bgColor = when (text) {
        "Popular" -> IntroPalette.PrimaryButton.copy(alpha = 0.25f)
        "New" -> IntroPalette.AccentLime.copy(alpha = 0.2f)
        else -> IntroPalette.SecondaryText.copy(alpha = 0.15f)
    }
    val textColor = when (text) {
        "Popular" -> IntroPalette.SecondaryText
        "New" -> IntroPalette.AccentLime
        else -> IntroPalette.SecondaryText
    }
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, color = textColor, style = IntroTypography.badge)
    }
}

@Composable
private fun HomeUpgradeCard(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntroPalette.SurfaceRaised, SectionShape)
            .border(1.dp, IntroPalette.BorderSubtle, SectionShape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Upgrade to Pro", style = IntroTypography.upgradeTitle)
        Text(
            text = "Unlock priority queue, HD output, and more credits every month.",
            style = IntroTypography.body,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        LumoraIntroPrimaryButton(
            text = "View plans",
            onClick = { onNavigate(Screen.Subscription.route) }
        )
    }
}
