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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.bgStudioRoute
import com.deep.lumoraai.core.restrictions.GenerationGate
import kotlinx.coroutines.launch

private val HomeBackground = Color(0xFF081020)
private val HomeCard = Color(0xFF10192D)
private val HomeStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Pink = Color(0xFFFF3D9D)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

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
        containerColor = HomeBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavigationBar(emptyList(), "home", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBackground)
                .padding(padding)
        ) {
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
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HomeTopBar(
            userName = uiState.userName,
            credits = uiState.credits,
            onNavigate = onNavigate
        )
        HomeHero(onExploreRecent = { onNavigate(Screen.History.route) })
        MainCreateGrid(onNavigate = onNavigate, onComingSoon = onComingSoon)
        RecentCreationsSection(items = uiState.recentItems, onNavigate = onNavigate)
        ToolsSection(onNavigate = onNavigate, onComingSoon = onComingSoon)
        Spacer(modifier = Modifier.height(2.dp))
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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF2D77FF), CircleShape)
                    .clickable { onNavigate(Screen.Profile.route) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_avatar),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Hi, ${userName.ifBlank { "Alex" }}",
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Good Morning",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CreditsChip(credits = credits, onClick = { onNavigate(Screen.Credits.route) })
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clickable { onNavigate(Screen.Notifications.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFFDFF7F4),
                    modifier = Modifier.size(23.dp)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .align(Alignment.TopEnd)
                        .background(Lime, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun CreditsChip(credits: Int, onClick: () -> Unit) {
    val label = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        "$credits"
    }
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("✦", color = Lime, fontSize = 13.sp, lineHeight = 13.sp)
        Text(label, color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HomeHero(onExploreRecent: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("What will we", color = Muted, fontSize = 14.sp, lineHeight = 18.sp)
            Text("Create", color = Color.White, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text("Today?", color = Lime, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text("Turn ideas into stunning visuals", color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Box(modifier = Modifier.width(34.dp).height(2.dp).background(Lime))
                Box(modifier = Modifier.width(34.dp).height(2.dp).background(Purple))
            }
        }
        HeroPreviewArt(onClick = onExploreRecent)
    }
}

@Composable
private fun HeroPreviewArt(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(106.dp)
            .height(98.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF6E35E7), Color(0xFF24163F), Color(0xFF10182D))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.group_48096841),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.88f),
            modifier = Modifier
                .size(34.dp)
                .shadow(10.dp, CircleShape)
        )
    }
}

@Composable
private fun MainCreateGrid(
    onNavigate: (String) -> Unit,
    onComingSoon: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text("Create", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
            CreateActionCard("Text → Image", "Dream it", Icons.Default.AutoAwesome, Lime, { onNavigate(Screen.TextToImage.route) }, Modifier.weight(1f))
            CreateActionCard("Img → Img", "Refine it", Icons.Default.Image, Purple, { onNavigate(Screen.ImageToImage.route) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
            CreateActionCard("Img → Video", "Animate it", Icons.Default.Movie, Pink, { onNavigate(Screen.ImageToVideo.route) }, Modifier.weight(1f))
            CreateActionCard("Text → Video", "Direct it", Icons.Default.PlayArrow, Cyan, { onNavigate(Screen.TextToVideo.route) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CreateActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(118.dp),
        shape = CardShape,
        color = HomeCard,
        border = BorderStroke(1.dp, HomeStroke.copy(alpha = 0.72f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF18243C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(21.dp))
            }
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun RecentCreationsSection(
    items: List<HomeRecentItem>,
    onNavigate: (String) -> Unit,
) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                text = "View all",
                color = Lime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigate(Screen.History.route) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            items.take(2).forEach { item ->
                RecentCreationCard(item = item, onClick = { onNavigate(Screen.History.route) }, modifier = Modifier.weight(1f))
            }
            if (items.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecentCreationCard(
    item: HomeRecentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(12.dp),
        color = HomeCard,
        border = BorderStroke(1.dp, HomeStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Image(
                painter = painterResource(id = item.fallbackImageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(9.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(item.timeLabel, color = Muted, fontSize = 11.sp, lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun ToolsSection(
    onNavigate: (String) -> Unit,
    onComingSoon: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tools", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SmallToolCard("AI BG", Icons.Default.AutoAwesome, Cyan, { onNavigate(bgStudioRoute("replace")) }, Modifier.weight(1f))
            SmallToolCard("Enhance", Icons.Default.Tune, Purple, { onNavigate(Screen.PhotoEnhance.route) }, Modifier.weight(1f))
            SmallToolCard("Remove", Icons.Default.PhotoCamera, Color(0xFF7D86FF), { onNavigate(bgStudioRoute("remove")) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SmallToolCard("Promo Video", Icons.Default.VideoLibrary, Pink, { onNavigate(Screen.PromoVideo.route) }, Modifier.weight(1f))
            SmallToolCard("Compress", Icons.Default.Compress, Lime, { onNavigate(Screen.Compress.route) }, Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SmallToolCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = CardShape,
        color = HomeCard,
        border = BorderStroke(1.dp, HomeStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
