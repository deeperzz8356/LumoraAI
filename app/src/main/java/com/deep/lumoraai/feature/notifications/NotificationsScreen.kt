package com.deep.lumoraai.feature.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.feature.notifications.model.NotificationModel
import com.deep.lumoraai.feature.notifications.model.NotificationType
import androidx.compose.ui.res.stringResource

private val NotificationBackground = Color(0xFF081020)
private val NotificationCard = Color(0xFF10192D)
private val NotificationStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Pink = Color(0xFFFF3D9D)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClicked: (NotificationModel) -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NotificationBackground,
        bottomBar = { BottomNavigationBar(emptyList(), "notifications", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NotificationBackground)
                .padding(padding)
        ) {
            when (uiState) {
                NotificationsUiState.Loading -> AppLoadingScreen()
                is NotificationsUiState.Error -> AppErrorScreen(message = uiState.message)
                is NotificationsUiState.Empty -> EmptyNotifications(
                    notificationsEnabled = uiState.notificationsEnabled,
                    onBack = onBack,
                    onNavigate = onNavigate,
                    onClearDismissed = onClearDismissed
                )
                is NotificationsUiState.Success -> NotificationsContent(
                    uiState = uiState,
                    onBack = onBack,
                    onNavigate = onNavigate,
                    onMarkAllRead = onMarkAllRead,
                    onNotificationClicked = onNotificationClicked,
                    onDismissNotification = onDismissNotification,
                    onClearDismissed = onClearDismissed
                )
            }
        }
    }
}

@Composable
private fun NotificationsContent(
    uiState: NotificationsUiState.Success,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClicked: (NotificationModel) -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearDismissed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PageTopBar(onBack = onBack)
        NotificationHero(
            unreadCount = uiState.unreadCount,
            notificationsEnabled = uiState.notificationsEnabled,
            onSettings = { onNavigate(Screen.Settings.route) },
            onMarkAllRead = onMarkAllRead
        )

        Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionCard("Queue", "Active renders", Icons.Default.PlayArrow, Pink, { onNavigate(Screen.Queue.route) }, Modifier.weight(1f))
            QuickActionCard("History", "Finished work", Icons.Default.TaskAlt, Lime, { onNavigate(Screen.History.route) }, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(com.deep.lumoraai.R.string.ui_inbox), color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Restore dismissed",
                color = Lime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onClearDismissed)
            )
        }

        uiState.items.forEach { item ->
            NotificationCard(
                item = item,
                onClick = { onNotificationClicked(item) },
                onDismiss = { onDismissNotification(item.id) }
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun EmptyNotifications(
    notificationsEnabled: Boolean,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onClearDismissed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PageTopBar(onBack = onBack)
        NotificationHero(
            unreadCount = 0,
            notificationsEnabled = notificationsEnabled,
            onSettings = { onNavigate(Screen.Settings.route) },
            onMarkAllRead = {}
        )
        Surface(
            modifier = Modifier.fillMaxWidth().height(136.dp),
            shape = CardShape,
            color = NotificationCard,
            border = BorderStroke(1.dp, NotificationStroke.copy(alpha = 0.72f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Lime, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(com.deep.lumoraai.R.string.ui_all_caught_up), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(com.deep.lumoraai.R.string.ui_new_generation_and_account_updates_will_appear_here), color = Muted, fontSize = 12.sp)
            }
        }
        Text(
            "Restore dismissed notifications",
            color = Lime,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable(onClick = onClearDismissed)
        )
    }
}

@Composable
private fun PageTopBar(onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.deep.lumoraai.R.string.ui_back), tint = Color.White)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(stringResource(com.deep.lumoraai.R.string.ui_notifications), color = Color.White, fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(com.deep.lumoraai.R.string.ui_updates_from_your_studio), color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun NotificationHero(
    unreadCount: Int,
    notificationsEnabled: Boolean,
    onSettings: () -> Unit,
    onMarkAllRead: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(190.dp),
        shape = CardShape,
        color = NotificationCard,
        border = BorderStroke(1.dp, Lime.copy(alpha = 0.32f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            Column(modifier = Modifier.fillMaxWidth(0.72f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (notificationsEnabled) "Inbox Live" else "Notifications Off", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                Text("$unreadCount unread", color = Color.White, fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    if (notificationsEnabled) "Track renders, credits, and account updates."
                    else "Enable alerts in Settings for generation updates.",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PillAction("Mark read", Icons.Default.Check, Lime, onMarkAllRead)
                PillAction("Settings", Icons.Default.Settings, Purple, onSettings)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Cyan.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Cyan, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, accent: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(82.dp),
        shape = CardShape,
        color = NotificationCard,
        border = BorderStroke(1.dp, NotificationStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccentIcon(icon, accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = Color.White, fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = Muted, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = accent, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationModel,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val accent = when (item.type) {
        NotificationType.Generation -> Pink
        NotificationType.Credits -> Lime
        NotificationType.Account -> Purple
        NotificationType.System -> Cyan
    }
    val icon = when (item.type) {
        NotificationType.Generation -> Icons.Default.PlayArrow
        NotificationType.Credits -> Icons.Default.CreditCard
        NotificationType.Account -> Icons.Default.Person
        NotificationType.System -> Icons.Default.Notifications
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = if (item.isRead) NotificationCard.copy(alpha = 0.72f) else NotificationCard,
        border = BorderStroke(1.dp, if (item.isRead) NotificationStroke.copy(alpha = 0.5f) else accent.copy(alpha = 0.46f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                AccentIcon(icon, accent)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        if (!item.isRead) Box(modifier = Modifier.size(7.dp).background(Lime, CircleShape))
                        Text(item.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                    Text(item.message, color = Muted, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(item.timeLabel, color = Color.White.copy(alpha = 0.48f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(com.deep.lumoraai.R.string.ui_dismiss), tint = Color.White.copy(alpha = 0.62f), modifier = Modifier.size(17.dp))
                }
            }
            item.progress?.let { progress ->
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = accent,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
            }
        }
    }
}

@Composable
private fun PillAction(label: String, icon: ImageVector, accent: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(17.dp))
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
