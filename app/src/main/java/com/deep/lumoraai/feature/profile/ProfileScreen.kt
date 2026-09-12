package com.deep.lumoraai.feature.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.PremiumActionRow
import com.deep.lumoraai.core.components.PremiumBackground
import com.deep.lumoraai.core.components.PremiumDanger
import com.deep.lumoraai.core.components.PremiumLime
import com.deep.lumoraai.core.components.PremiumMuted
import com.deep.lumoraai.core.components.PremiumPage
import com.deep.lumoraai.core.components.PremiumSection
import com.deep.lumoraai.core.components.PremiumStroke
import com.deep.lumoraai.core.components.PremiumSurface
import com.deep.lumoraai.core.components.PremiumText
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.data.model.HistoryModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File

private enum class ProfileAction { Delete, SignOut, Login }

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onNext: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    unreadCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PremiumBackground,
        bottomBar = { BottomNavigationBar(emptyList(), "profile", onNavigate) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (uiState) {
                ProfileUiState.Loading -> AppLoadingScreen()
                ProfileUiState.Empty -> AppErrorScreen("Your creator profile is not available yet.")
                is ProfileUiState.Error -> AppErrorScreen(uiState.message)
                is ProfileUiState.Success -> ProfileContent(
                    state = uiState,
                    unreadCount = unreadCount,
                    onNavigate = onNavigate,
                    onBack = onBack,
                    onSignOut = onSignOut,
                    onDeleteAccount = onDeleteAccount,
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Success,
    unreadCount: Int,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val saved = remember(user?.uid) { ProfilePreferences.load(context, user) }
    val name = saved.fullName.ifBlank { state.items.getOrNull(0).orEmpty() }.ifBlank { "Lumora Creator" }
    val username = saved.username.ifBlank { state.items.getOrNull(1).orEmpty().removePrefix("@") }
    val plan = state.items.getOrNull(2).orEmpty().ifBlank { if (state.isGuest) "Guest creator" else "Lumora creator" }
    var pendingAction by remember { mutableStateOf<ProfileAction?>(null) }

    pendingAction?.let { action ->
        ProfileActionDialog(
            action = action,
            onDismiss = { pendingAction = null },
            onConfirm = {
                pendingAction = null
                when (action) {
                    ProfileAction.Delete -> onDeleteAccount()
                    ProfileAction.SignOut -> onSignOut()
                    ProfileAction.Login -> onNavigate(Screen.Auth.route)
                }
            },
        )
    }

    PremiumPage(
        title = "Profile",
        eyebrow = "Creator space",
        subtitle = "Your work, credits and account",
        onBack = onBack,
        action = {
            Box {
                IconButton(onClick = { onNavigate(Screen.Notifications.route) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Notifications, "Open notifications", tint = PremiumText)
                }
                if (unreadCount > 0) Box(Modifier.align(Alignment.TopEnd).size(9.dp).clip(CircleShape).background(PremiumLime))
            }
        },
    ) {
        ProfileHero(name, username, plan, saved.avatarUri) {
            onNavigate(if (user == null || user.isAnonymous) Screen.Auth.route else Screen.EditProfile.route)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileStat("Credits", state.credits.toString(), Icons.Default.CreditCard, Modifier.weight(1f)) { onNavigate(Screen.Credits.route) }
            ProfileStat("Creations", state.generations.size.toString(), Icons.Default.GridView, Modifier.weight(1f)) { onNavigate(Screen.History.route) }
        }
        PremiumSection("Recent work", "Your latest Lumora creations") {
            if (state.generations.isEmpty()) {
                PremiumActionRow("Start your first creation", "Generate an image or video from Home", Icons.Default.AutoAwesome, { onNavigate(Screen.TextToImage.route) })
            } else {
                state.generations.take(4).chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { item -> CreationCard(item, Modifier.weight(1f)) { onNavigate(Screen.History.route) } }
                        if (rowItems.size == 1) Box(Modifier.weight(1f))
                    }
                }
                TextButton(onClick = { onNavigate(Screen.History.route) }, modifier = Modifier.align(Alignment.End)) {
                    Text("View all creations", color = PremiumLime, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = PremiumLime, modifier = Modifier.padding(start = 6.dp).size(17.dp))
                }
            }
        }
        PremiumSection("Account", "Shortcuts and preferences") {
            PremiumActionRow("Account settings", "Preferences, language and billing", Icons.Default.Settings, { onNavigate(Screen.Settings.route) })
            PremiumActionRow("Privacy & security", "Data choices and account protection", Icons.Default.Security, { onNavigate(Screen.PrivacySecurity.route) })
            PremiumActionRow("Help & support", "Guides and direct support", Icons.AutoMirrored.Filled.HelpOutline, { onNavigate(Screen.HelpSupport.route) })
            PremiumActionRow(
                "Share profile",
                "Invite others to see your Lumora identity",
                Icons.Default.Share,
                {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "$name on LumoraAI")
                    }
                    context.startActivity(Intent.createChooser(share, context.getString(R.string.ui_share)))
                },
            )
            PremiumActionRow("Delete account", "Permanently remove your Lumora account", Icons.Default.DeleteOutline, { pendingAction = ProfileAction.Delete }, danger = true)
            PremiumActionRow(
                if (state.isGuest) "Log in" else "Sign out",
                if (state.isGuest) "Save your work across devices" else "End this session on this device",
                if (state.isGuest) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout,
                { pendingAction = if (state.isGuest) ProfileAction.Login else ProfileAction.SignOut },
                danger = !state.isGuest,
            )
        }
    }
}

@Composable
private fun ProfileHero(
    name: String,
    username: String,
    plan: String,
    avatarUri: String?,
    onEdit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = PremiumSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        shadowElevation = 12.dp,
    ) {
        Box(Modifier.background(Brush.linearGradient(listOf(Color(0xFF23274C), Color(0xFF151B2D), Color(0xFF0F1726))))) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "$name profile picture",
                        placeholder = painterResource(R.drawable.user_avatar),
                        error = painterResource(R.drawable.user_avatar),
                        modifier = Modifier.size(96.dp).clip(CircleShape).background(PremiumStroke),
                    )
                    Box(Modifier.align(Alignment.BottomEnd).size(25.dp).clip(CircleShape).background(PremiumLime).border(4.dp, PremiumSurface, CircleShape))
                }
                Text(name, color = PremiumText, fontSize = 25.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (username.isNotBlank()) Text("@${username.removePrefix("@")}", color = PremiumMuted, fontSize = 13.sp)
                Text(
                    plan.uppercase(),
                    color = PremiumLime,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    modifier = Modifier.background(PremiumLime.copy(alpha = 0.10f), RoundedCornerShape(8.dp)).padding(horizontal = 9.dp, vertical = 5.dp),
                )
                Button(
                    onClick = onEdit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumLime, contentColor = PremiumBackground),
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(17.dp))
                    Text("Edit profile", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 7.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(18.dp), color = PremiumSurface, border = BorderStroke(1.dp, PremiumStroke.copy(alpha = 0.7f))) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = PremiumLime, modifier = Modifier.size(22.dp))
            Text(value, color = PremiumText, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text(label.uppercase(), color = PremiumMuted, fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun CreationCard(item: HistoryModel, modifier: Modifier, onClick: () -> Unit) {
    val file = item.mediaUrl?.let(::File)
    val isVideo = item.type.equals("VIDEO", true)
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(17.dp), color = PremiumSurface, border = BorderStroke(1.dp, PremiumStroke.copy(alpha = 0.7f))) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.18f).background(PremiumStroke)) {
            AsyncImage(
                model = if (file?.exists() == true && !isVideo) Uri.fromFile(file) else null,
                contentDescription = item.title,
                placeholder = painterResource(if (isVideo) R.drawable.style_digital else R.drawable.style_fantasy),
                error = painterResource(if (isVideo) R.drawable.style_digital else R.drawable.style_fantasy),
                modifier = Modifier.fillMaxSize(),
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC080D18)))))
            if (isVideo) {
                Box(Modifier.align(Alignment.Center).size(38.dp).clip(CircleShape).background(PremiumLime), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = PremiumBackground)
                }
            }
            Text(
                item.title.ifBlank { if (isVideo) "Video" else "Image" },
                color = PremiumText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(10.dp),
            )
        }
    }
}

@Composable
private fun ProfileActionDialog(action: ProfileAction, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val title = when (action) {
        ProfileAction.Delete -> "Delete account?"
        ProfileAction.SignOut -> "Sign out?"
        ProfileAction.Login -> "Log in to save your work?"
    }
    val body = when (action) {
        ProfileAction.Delete -> "This permanently removes your account and cannot be undone."
        ProfileAction.SignOut -> "Your saved work remains available when you sign in again."
        ProfileAction.Login -> "Connect an account to keep creations and credits across devices."
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumSurface,
        titleContentColor = PremiumText,
        textContentColor = PremiumMuted,
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(if (action == ProfileAction.Delete) "Delete permanently" else if (action == ProfileAction.Login) "Log in" else "Sign out", color = if (action == ProfileAction.Login) PremiumLime else PremiumDanger) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = PremiumMuted) } },
    )
}
