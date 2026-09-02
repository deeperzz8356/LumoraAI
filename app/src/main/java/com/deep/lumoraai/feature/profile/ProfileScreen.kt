package com.deep.lumoraai.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.MediaViewerDialog
import com.deep.lumoraai.core.components.VideoFirstFrameThumbnail
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.data.model.HistoryModel
import java.io.File

private val ProfileBackground = Color(0xFF081020)
private val ProfileCard = Color(0xFF10192D)
private val ProfileStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Pink = Color(0xFFFF3D9D)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNext: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ProfileBackground,
        bottomBar = { BottomNavigationBar(emptyList(), "profile", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ProfileBackground)
                .padding(padding)
        ) {
            when (uiState) {
                ProfileUiState.Loading -> AppLoadingScreen()
                is ProfileUiState.Error -> AppErrorScreen(message = uiState.message)
                ProfileUiState.Empty -> AppEmptyScreen(title = "Profile", body = "No account details available.")
                is ProfileUiState.Success -> ProfileContent(
                    credits = uiState.credits,
                    generations = uiState.generations,
                    onSignOut = onSignOut,
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    credits: Int,
    generations: List<HistoryModel>,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProfileTopBar(onNavigate = onNavigate)
        ProfileHero(onNavigate = onNavigate)

        Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardCard("$credits", "LUM credits", Icons.Default.Star, Lime, { onNavigate(Screen.Credits.route) }, Modifier.weight(1f))
            DashboardCard("${generations.size}", "creations", Icons.Default.GridView, Purple, { onNavigate(Screen.History.route) }, Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
            ShortcutCard("Manage Plan", "Elite Pro", Icons.AutoMirrored.Filled.ReceiptLong, Pink, { onNavigate(Screen.Subscription.route) }, Modifier.weight(1f))
            ShortcutCard("Top Up", "Buy credits", Icons.Default.Add, Cyan, { onNavigate(Screen.Credits.route) }, Modifier.weight(1f))
        }

        CreationsSection(generations = generations, onNavigate = onNavigate)
        PurchaseHistoryCard(credits = credits, onNavigate = onNavigate)
        PreferencesList(onSignOut = onSignOut, onNavigate = onNavigate)
        SupportCard()
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun ProfileTopBar(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Avatar(size = 38.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Profile", color = Color.White, fontSize = 18.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold)
                Text("Account Settings", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp, lineHeight = 15.sp)
            }
        }
        Box(
            modifier = Modifier.size(34.dp).clickable { onNavigate(Screen.Notifications.route) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFFDFF7F4), modifier = Modifier.size(23.dp))
            Box(modifier = Modifier.size(6.dp).align(Alignment.TopEnd).background(Lime, CircleShape))
        }
    }
}

@Composable
private fun ProfileHero(onNavigate: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(164.dp),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Avatar(size = 70.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Alex Thorne", color = Color.White, fontSize = 21.sp, lineHeight = 25.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("@alexthorne_creatives", color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniAction("Edit", Icons.Default.Edit, Lime, onClick = { onNavigate(EDIT_PROFILE_ROUTE) })
                        MiniAction("Share", Icons.Default.Share, Purple, onClick = {})
                    }
                }
            }
            Box(modifier = Modifier.align(Alignment.BottomEnd).width(34.dp).height(2.dp).background(Lime))
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 34.dp).width(34.dp).height(2.dp).background(Purple))
        }
    }
}

@Composable
private fun Avatar(size: androidx.compose.ui.unit.Dp) {
    Box(modifier = Modifier.size(size).clip(CircleShape)) {
        Image(
            painter = painterResource(id = R.drawable.user_avatar),
            contentDescription = "Profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MiniAction(label: String, icon: ImageVector, accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(15.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DashboardCard(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(104.dp),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.58f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AccentIcon(icon, accent)
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(label, color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.58f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AccentIcon(icon, accent)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accent, modifier = Modifier.align(Alignment.TopEnd).size(17.dp))
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun CreationsSection(generations: List<HistoryModel>, onNavigate: (String) -> Unit) {
    val selected = remember { mutableStateOf<HistoryModel?>(null) }
    val viewing = selected.value

    if (viewing != null && !viewing.mediaUrl.isNullOrBlank()) {
        MediaViewerDialog(
            filePath = viewing.mediaUrl.orEmpty(),
            mediaType = viewing.type,
            title = viewing.title.ifBlank { if (viewing.type.equals("VIDEO", ignoreCase = true)) "Video" else "Image" },
            onDismiss = { selected.value = null },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("My Creations", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onNavigate(Screen.History.route) }) {
                Text("View all", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Lime, modifier = Modifier.size(16.dp))
            }
        }

        if (generations.isEmpty()) {
            Surface(
                onClick = { onNavigate(Screen.TextToImage.route) },
                modifier = Modifier.fillMaxWidth().height(76.dp),
                shape = CardShape,
                color = ProfileCard,
                border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.58f))
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccentIcon(Icons.Default.Add, Lime)
                    Column {
                        Text("Start your first creation", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Generate an image or video from Home", color = Muted, fontSize = 11.sp)
                    }
                }
            }
        } else {
            generations.take(4).chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { item ->
                        CreationCard(item = item, modifier = Modifier.weight(1f)) {
                            val path = item.mediaUrl
                            if (!path.isNullOrBlank() && File(path).exists()) selected.value = item
                        }
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CreationCard(item: HistoryModel, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val path = item.mediaUrl.orEmpty()
    val file = remember(path) { File(path) }

    Surface(
        onClick = onClick,
        modifier = modifier.height(116.dp),
        shape = RoundedCornerShape(12.dp),
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.58f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isVideo && path.isNotBlank() && file.exists()) {
                VideoFirstFrameThumbnail(filePath = path, contentDescription = item.title, fallbackImageRes = R.drawable.group_48096841, modifier = Modifier.fillMaxSize())
            } else if (!isVideo && path.isNotBlank() && file.exists()) {
                AsyncImage(model = file, contentDescription = item.title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black))
            }
            if (isVideo) {
                Box(modifier = Modifier.size(30.dp).background(Color.Black.copy(alpha = 0.45f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun PurchaseHistoryCard(credits: Int, onNavigate: (String) -> Unit) {
    InfoCard(title = "Purchase History", icon = Icons.AutoMirrored.Filled.List) {
        PurchaseRow("Current balance", "$credits LUM credits available", "Credits") { onNavigate(Screen.Credits.route) }
        PurchaseRow("Subscription receipts", "Plan status and billing actions", "Plan") { onNavigate(Screen.Subscription.route) }
        PurchaseRow("Creation usage", "Open saved renders and downloads", "History") { onNavigate(Screen.History.route) }
    }
}

@Composable
private fun PurchaseRow(title: String, desc: String, price: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(desc, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(price, color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Lime, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun PreferencesList(onSignOut: () -> Unit, onNavigate: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrefRow("Account Settings", Icons.Default.Settings, Color.White, onClick = { onNavigate(Screen.Settings.route) })
            PrefRow("Privacy Policy", Icons.Default.Info, Color.White)
            PrefRow("Terms of Service", Icons.Default.Info, Color.White)
            PrefRow("Delete Account", Icons.Default.Delete, Color(0xFFFF7A7A))
            PrefRow("Sign Out", Icons.AutoMirrored.Filled.ExitToApp, Color(0xFFFF7A7A), onClick = onSignOut)
        }
    }
}

@Composable
private fun PrefRow(title: String, icon: ImageVector, color: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(title, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SupportCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text("Need Help?", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Support, billing, and account questions.", color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
            }
            MiniAction("Support", Icons.Default.Info, Cyan, onClick = {})
        }
    }
}

@Composable
private fun InfoCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = Lime, modifier = Modifier.size(17.dp))
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
            content()
        }
    }
}

@Composable
private fun AccentIcon(icon: ImageVector, accent: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
    }
}
