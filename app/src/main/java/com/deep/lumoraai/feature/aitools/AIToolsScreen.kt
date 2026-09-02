package com.deep.lumoraai.feature.aitools

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.LumoraTopBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.bgStudioRoute

private val AIToolsBackground = Color(0xFF081020)
private val AIToolsCard = Color(0xFF10192D)
private val AIToolsStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Pink = Color(0xFFFF3D9D)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)
private val BlueAccent = Color(0xFF7D86FF)

@Composable
fun AIToolsScreen(
    credits: Int,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AIToolsBackground,
        bottomBar = { BottomNavigationBar(emptyList(), "aitools", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AIToolsBackground)
                .padding(padding)
        ) {
            AIToolsContent(credits = credits, onNavigate = onNavigate)
        }
    }
}

@Composable
private fun AIToolsContent(
    credits: Int,
    onNavigate: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LumoraTopBar(
            credits = credits,
            title = "AI Tools",
            onProfileClick = { onNavigate(Screen.Profile.route) },
            onCreditsClick = { onNavigate(Screen.Credits.route) },
            onNotificationsClick = { onNavigate(Screen.Notifications.route) },
        )
        Text(
            "Explore all the powerful tools to transform your content",
            color = Muted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 18.sp
        )

        Text(
            "Tools",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToolBentoCard(
                title = "AI Background Replace",
                subtitle = "Generate a new scene",
                icon = Icons.Default.AutoAwesome,
                accent = Cyan,
                onClick = { onNavigate(bgStudioRoute("replace")) },
                modifier = Modifier
                    .weight(1f)
                    .height(116.dp),
                prominent = true
            )
            ToolBentoCard(
                title = "Photo Enhancer",
                subtitle = "Improve quality",
                icon = Icons.Default.Tune,
                accent = Purple,
                onClick = { onNavigate(Screen.PhotoEnhance.route) },
                modifier = Modifier
                    .weight(1f)
                    .height(116.dp),
                prominent = true
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ToolBentoCard(
                title = "Promo Videos",
                subtitle = "Ad-ready clips",
                icon = Icons.Default.VideoLibrary,
                accent = Pink,
                onClick = { onNavigate(Screen.PromoVideo.route) },
                modifier = Modifier
                    .weight(1f)
                    .height(116.dp),
                prominent = true
            )
            ToolBentoCard(
                title = "Remove Background",
                subtitle = "Cut subject",
                icon = Icons.Default.PhotoCamera,
                accent = BlueAccent,
                onClick = { onNavigate(bgStudioRoute("remove")) },
                modifier = Modifier
                    .weight(1f)
                    .height(116.dp),
                prominent = true
            )
        }
        ToolBentoCard(
            title = "Compress",
            subtitle = "Images and videos",
            icon = Icons.Default.Compress,
            accent = Lime,
            onClick = { onNavigate(Screen.Compress.route) },
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp),
            prominent = true
        )

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun AIToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CardShape,
        color = AIToolsCard,
        border = BorderStroke(1.dp, AIToolsStroke.copy(alpha = 0.72f))
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
                    .clip(RoundedCornerShape(50))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
            }
        }
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
        color = AIToolsCard,
        border = BorderStroke(1.dp, AIToolsStroke.copy(alpha = 0.58f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (prominent) 14.dp else 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (prominent) 38.dp else 32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(if (prominent) 23.dp else 19.dp))
            }
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = if (prominent) 15.sp else 14.sp,
                    lineHeight = if (prominent) 18.sp else 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    maxLines = if (prominent) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Muted,
                    fontSize = if (prominent) 11.sp else 11.sp,
                    lineHeight = if (prominent) 13.sp else 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
