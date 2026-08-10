package com.deep.lumoraai.feature.profile

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
import androidx.compose.material3.Icon
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
import com.deep.lumoraai.core.components.MediaViewerDialog
import com.deep.lumoraai.data.model.HistoryModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star

import com.deep.lumoraai.core.navigation.Screen
import java.io.File

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNext: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf<HistoryModel?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "profile",
                onSelected = onNavigate
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF0F1026), Color(0xFF070714))))
        ) {
            ProfileContent(
                uiState = uiState,
                onSignOut = onSignOut,
                onNavigate = onNavigate,
                onOpenMedia = { selectedItem = it },
            )
        }
    }

    selectedItem?.let { item ->
        val path = item.mediaUrl
        if (!path.isNullOrBlank()) {
            MediaViewerDialog(
                filePath = path,
                mediaType = item.type,
                mimeType = if (item.type.equals("VIDEO", ignoreCase = true)) "video/mp4" else "image/png",
                title = item.title,
                onDismiss = { selectedItem = null },
            )
        } else {
            selectedItem = null
        }
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit,
    onOpenMedia: (HistoryModel) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProfileTopBar()
        ProfileHeader()
        val credits = if (uiState is ProfileUiState.Success) uiState.credits else 0
        CreditsBalanceCard(credits = credits)
        PlanCard(onNavigate = onNavigate)
        BuyMoreCreditsCard(onNavigate = onNavigate)
        if (uiState is ProfileUiState.Success) {
            CreationsGrid(
                generations = uiState.generations,
                onOpenMedia = onOpenMedia,
                onNavigate = onNavigate,
            )
        }
        PurchaseHistoryCard()
        PreferencesList(onSignOut = onSignOut, onNavigate = onNavigate)
        SupportCard()
    }
}

@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Lumina AI", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Image(
            painter = painterResource(id = R.drawable.user_avatar),
            contentDescription = null,
            modifier = Modifier.size(36.dp).clip(CircleShape)
        )
    }
}

@Composable
private fun ProfileHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Image(
                painter = painterResource(id = R.drawable.user_avatar),
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(CircleShape).border(2.dp, Color(0xFFA855F7), CircleShape)
            )
            Box(
                modifier = Modifier
                    .background(Color(0xFFA855F7), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("PRO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text("Alex Thorne", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("@alexthorne_creatives", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TagPill("Concept Artist")
            TagPill("Video Director")
        }
        ProfileHeaderActions()
    }
}

@Composable
private fun TagPill(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
private fun ProfileHeaderActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = {},
            modifier = Modifier.weight(1f).height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E50EF)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit Profile", color = Color.White, fontSize = 13.sp)
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CreditsBalanceCard(credits: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Credits Balance", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Text("$credits LUM", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight().background(Color(0xFFCFBDFF), CircleShape))
        }
        Text("Next refill in 12 days", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
    }
}

@Composable
private fun PlanCard(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Active Plan", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Elite Pro", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Billed annually • $499/yr", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigate(Screen.Subscription.route) }
            ) {
                Text("Manage Plan", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun BuyMoreCreditsCard(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable { onNavigate(Screen.Credits.route) }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Column {
            Text("Buy More Credits", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Instant refill for emergency generations.", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun CreationsGrid(
    generations: List<HistoryModel>,
    onOpenMedia: (HistoryModel) -> Unit,
    onNavigate: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Saved Creations", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigate(Screen.History.route) }
            ) {
                Text("View Gallery", color = Color(0xFFA855F7), fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
            }
        }

        if (generations.isEmpty()) {
            Text("You haven't generated any media yet.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        } else {
            val displayItems = generations.take(4)
            val rows = displayItems.chunked(2)

            for (rowItems in rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    for (item in rowItems) {
                        CreationCard(
                            item = item,
                            onClick = { onOpenMedia(item) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CreationCard(
    item: HistoryModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val path = item.mediaUrl
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .background(Color.Black)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!path.isNullOrBlank() && File(path).exists() && !isVideo) {
            coil.compose.AsyncImage(
                model = File(path),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (isVideo) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Text("No media", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun PurchaseHistoryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.List, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("PURCHASE HISTORY", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        PurchaseRow("Pro Annual Plan", "Nov 12, 2023 • Inv #6921", "$499.00")
        PurchaseRow("1,000 Credit Pack", "Oct 28, 2023 • Inv #7741", "$49.00")
        PurchaseRow("Early Adopter Credit Bonus", "Sep 15, 2023 • Promo", "FREE")
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Text("Download All Receipts", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PurchaseRow(title: String, desc: String, price: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        }
        Text(price, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PreferencesList(onSignOut: () -> Unit, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("PREFERENCES", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        PrefRow("Account Settings", Icons.Default.Settings, Color.White, onClick = { onNavigate(Screen.Settings.route) })
        PrefRow("Privacy Policy", Icons.Default.Share, Color.White)
        PrefRow("Terms of Service", Icons.Default.Info, Color.White)
        PrefRow("Delete Account", Icons.Default.Delete, Color(0xFFFF7A7A))
        PrefRow("Sign Out", Icons.Default.ExitToApp, Color(0xFFFF7A7A), onSignOut)
    }
}

@Composable
private fun PrefRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SupportCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text("Need Help?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Our concierge support is available 24/7.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, lineHeight = 14.sp)
        }
        Button(
            onClick = {},
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
        ) {
            Text("Support Chat", color = Color.White, fontSize = 11.sp)
        }
    }
}