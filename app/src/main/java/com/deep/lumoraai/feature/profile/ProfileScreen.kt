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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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
import com.deep.lumoraai.core.components.MediaViewerDialog
import com.deep.lumoraai.data.model.HistoryModel
import coil.compose.AsyncImage
import java.io.File
import com.deep.lumoraai.core.navigation.Screen

// Home page colors and styling (matching aesthetic)
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
                .background(ProfileBackground)
                .padding(padding)
        ) {
            ProfileContent(uiState = uiState, onSignOut = onSignOut, onNavigate = onNavigate)
        }
    }
}

@Composable
private fun ProfileContent(uiState: ProfileUiState, onSignOut: () -> Unit, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ProfileTopBar(onNavigate = onNavigate)
        ProfileHeader()
        val credits = if (uiState is ProfileUiState.Success) uiState.credits else 0
        CreditsBalanceCard(credits = credits)
        PlanCard(onNavigate = onNavigate)
        BuyMoreCreditsCard(onNavigate = onNavigate)
        if (uiState is ProfileUiState.Success) {
            CreationsGrid(generations = uiState.generations)
        }
        PurchaseHistoryCard()
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
                    text = "Profile",
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "Account Settings",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader() {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFF2D77FF), CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.user_avatar),
                contentDescription = "Profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Alex Thorne", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("@alexthorne_creatives", color = Muted, fontSize = 12.sp)
        }
        ProfileHeaderActions()
    }
}

@Composable
private fun ProfileHeaderActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            onClick = {},
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = CardShape,
            color = ProfileCard,
            border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Lime, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Profile", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Surface(
            onClick = {},
            modifier = Modifier
                .size(44.dp),
            shape = CircleShape,
            color = ProfileCard,
            border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Purple, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun CreditsBalanceCard(credits: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Credits Balance", color = Muted, fontSize = 12.sp)
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Text("$credits LUM", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Box(modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight().background(Lime.copy(alpha = 0.5f), CircleShape))
            }
            Text("Next refill in 12 days", color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PlanCard(onNavigate: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Active Plan", color = Muted, fontSize = 12.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Elite Pro", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Billed annually • $499/yr", color = Muted, fontSize = 11.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigate(Screen.Subscription.route) }
                ) {
                    Text("Manage Plan", color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Lime, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun BuyMoreCreditsCard(onNavigate: (String) -> Unit) {
    Surface(
        onClick = { onNavigate(Screen.Credits.route) },
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp))
            Column {
                Text("Buy More Credits", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Instant refill for emergency generations.", color = Muted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun CreationsGrid(generations: List<HistoryModel>) {
    val selected = remember { mutableStateOf<HistoryModel?>(null) }
    val viewing = selected.value

    if (viewing != null && !viewing.mediaUrl.isNullOrBlank()) {
        MediaViewerDialog(
            filePath = viewing.mediaUrl.orEmpty(),
            mediaType = viewing.type,
            title = viewing.title.ifBlank {
                if (viewing.type.equals("VIDEO", ignoreCase = true)) "Video" else "Image"
            },
            onDismiss = { selected.value = null },
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("My Creations", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val path = item.mediaUrl
                                if (!path.isNullOrBlank() && File(path).exists()) {
                                    selected.value = item
                                }
                            },
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isVideo = item.type.equals("VIDEO", ignoreCase = true)
    val path = item.mediaUrl
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.58f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (!isVideo && !path.isNullOrBlank() && File(path).exists()) {
                AsyncImage(
                    model = File(path),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVideo) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            tint = Lime,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseHistoryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.List, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("PURCHASE HISTORY", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp)
            }
            PurchaseRow("Pro Annual Plan", "Nov 12, 2023 • Inv #6921", "$499.00")
            PurchaseRow("1,000 Credit Pack", "Oct 28, 2023 • Inv #7741", "$49.00")
            PurchaseRow("Early Adopter Credit Bonus", "Sep 15, 2023 • Promo", "FREE")
            Surface(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = CardShape,
                color = ProfileCard,
                border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Download All Receipts", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
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
            Text(desc, color = Muted, fontSize = 10.sp)
        }
        Text(price, color = Lime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PreferencesList(onSignOut: () -> Unit, onNavigate: (String) -> Unit) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    if (showDeleteDialog.value) {
        DeleteAccountDialog(
            onConfirm = {
                showDeleteDialog.value = false
                // TODO: Add delete account logic here
            },
            onDismiss = { showDeleteDialog.value = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = ProfileCard,
        border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("PREFERENCES", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp)
            PrefRow("Account Settings", Icons.Default.Settings, Color.White, onClick = { onNavigate(Screen.Settings.route) })
            PrefRow("Privacy Policy", Icons.Default.Info, Color.White)
            PrefRow("Terms of Service", Icons.Default.Info, Color.White)
            PrefRow("Delete Account", Icons.Default.Delete, Color(0xFFFF7A7A), onClick = { showDeleteDialog.value = true })
            PrefRow("Sign Out", Icons.Default.ExitToApp, Color(0xFFFF7A7A), onSignOut)
        }
    }
}

@Composable
private fun PrefRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text("Need Help?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Our concierge support is available 24/7.", color = Muted, fontSize = 10.sp, lineHeight = 14.sp)
            }
            Surface(
                onClick = {},
                modifier = Modifier.height(36.dp),
                shape = CardShape,
                color = ProfileCard.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.58f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text("Support", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(16.dp),
            color = ProfileCard,
            border = BorderStroke(1.dp, ProfileStroke.copy(alpha = 0.72f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0xFFFF7A7A),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "Delete Account?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "This action cannot be undone. All your data, credits, and history will be permanently deleted.",
                    color = Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ProfileCard.copy(alpha = 0.7f))
                    ) {
                        Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A7A))
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}