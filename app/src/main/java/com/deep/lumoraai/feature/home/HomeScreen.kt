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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.deep.lumoraai.core.components.LumoraNotificationBell
import com.deep.lumoraai.core.components.VideoFirstFrameThumbnail
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.bgStudioRoute
import com.deep.lumoraai.core.restrictions.GenerationGate
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.res.stringResource

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
    unreadCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = HomeBackground,
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
                is HomeUiState.Empty -> AppEmptyScreen(title = stringResource(com.deep.lumoraai.R.string.ui_no_content), body = "Nothing to see here.")
                is HomeUiState.Success -> HomeContent(
                    uiState = uiState,
                    onNavigate = onNavigate,
                    unreadCount = unreadCount,
                    onNotificationClick = onNotificationClick,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState.Success,
    onNavigate: (String) -> Unit,
    unreadCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
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
            onNavigate = onNavigate,
            unreadCount = unreadCount,
            onNotificationClick = onNotificationClick
        )
        HomeHero(onExploreRecent = { onNavigate(Screen.History.route) })
        MainCreateGrid(onNavigate = onNavigate)
        RecentCreationsSection(items = uiState.recentItems, onNavigate = onNavigate)
        ToolsSection(onNavigate = onNavigate)
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun HomeTopBar(
    userName: String,
    credits: Int,
    onNavigate: (String) -> Unit,
    unreadCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null,
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
                    contentDescription = stringResource(com.deep.lumoraai.R.string.ui_profile),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Hi, ${userName.ifBlank { "guest" }}",
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
            LumoraNotificationBell(
                hasUnreadNotifications = unreadCount > 0,
                onClick = {
                    onNotificationClick?.invoke()
                        ?: onNavigate(Screen.Notifications.route)
                }
            )
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
            Text(stringResource(com.deep.lumoraai.R.string.ui_what_will_we), color = Muted, fontSize = 14.sp, lineHeight = 18.sp)
            Text(stringResource(com.deep.lumoraai.R.string.ui_create), color = Color.White, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(com.deep.lumoraai.R.string.ui_today), color = Lime, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
            Text(stringResource(com.deep.lumoraai.R.string.ui_turn_ideas_into_stunning_visuals), color = Muted, fontSize = 13.sp, lineHeight = 20.sp)
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
) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text(stringResource(com.deep.lumoraai.R.string.ui_create), color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
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
            Text(stringResource(com.deep.lumoraai.R.string.ui_recent), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                text = "View all",
                color = Lime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigate(Screen.History.route) }
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                RecentCreationCard(
                    item = item,
                    onClick = { onNavigate(Screen.History.route) },
                    modifier = Modifier.width(190.dp)
                )
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
    val isVideo = item.mediaType.equals("VIDEO", ignoreCase = true)
    val mediaPath = item.mediaUrl.orEmpty()
    val mediaFile = remember(mediaPath) { File(mediaPath) }

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
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isVideo && mediaPath.isNotBlank() && mediaFile.exists()) {
                    VideoFirstFrameThumbnail(
                        filePath = mediaPath,
                        contentDescription = item.title,
                        fallbackImageRes = item.fallbackImageRes,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!isVideo && mediaPath.isNotBlank() && mediaFile.exists()) {
                    AsyncImage(
                        model = mediaFile,
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
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
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
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(stringResource(com.deep.lumoraai.R.string.ui_tools), color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ToolBentoCard(
                title = "AI Background\nReplace",
                subtitle = "",
                icon = Icons.Default.AutoAwesome,
                accent = Cyan,
                onClick = { onNavigate(bgStudioRoute("replace")) },
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                prominent = true
            )
            ToolBentoCard(
                title = "Photo\nEnhancer",
                subtitle = "",
                icon = Icons.Default.Tune,
                accent = Purple,
                onClick = { onNavigate(Screen.PhotoEnhance.route) },
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                prominent = true
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ToolBentoCard(
                title = "Promo\nVideos",
                subtitle = "",
                icon = Icons.Default.VideoLibrary,
                accent = Pink,
                onClick = { onNavigate(Screen.PromoVideo.route) },
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                prominent = true
            )
            ToolBentoCard(
                title = "Remove\nBackground",
                subtitle = "",
                icon = Icons.Default.PhotoCamera,
                accent = Color(0xFF7D86FF),
                onClick = { onNavigate(bgStudioRoute("remove")) },
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp),
                prominent = true
            )
        }
        ToolBentoCard(
            title = "Compress",
            subtitle = "",
            icon = Icons.Default.Compress,
            accent = Lime,
            onClick = { onNavigate(Screen.Compress.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            prominent = true
        )
    }
}

@Composable
private fun ToolBentoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prominent: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        color = HomeCard,
        border = BorderStroke(1.dp, HomeStroke.copy(alpha = 0.58f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(if (prominent) 44.dp else 36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(if (prominent) 26.dp else 20.dp))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = if (prominent) 16.sp else 15.sp,
                        lineHeight = if (prominent) 20.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = Muted,
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
