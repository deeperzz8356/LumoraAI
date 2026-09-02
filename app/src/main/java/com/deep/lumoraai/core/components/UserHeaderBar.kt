package com.deep.lumoraai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.theme.IntroPalette
import com.deep.lumoraai.core.theme.LumoraTheme

/**
 * Global header bar showing page title, user credits, and notifications
 * Used across all main screens except auth flows
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHeaderBar(
    title: String,
    modifier: Modifier = Modifier,
    userCredits: Int = 0,
    unreadNotificationCount: Int = 0,
    onNotifications: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            // Credits Badge
            Surface(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = IntroPalette.AccentLime,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "💰",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$userCredits",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Notification Bell
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onNotifications)
                    .padding(8.dp)
            ) {
                // Bell icon
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = "Notifications",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                // Badge with unread count
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    )
}

@Preview(name = "UserHeaderBar - Default")
@Composable
private fun UserHeaderBarPreview() {
    LumoraTheme {
        UserHeaderBar(
            title = "Home",
            userCredits = 500,
            unreadNotificationCount = 3
        )
    }
}

@Preview(name = "UserHeaderBar - With Back")
@Composable
private fun UserHeaderBarWithBackPreview() {
    LumoraTheme {
        UserHeaderBar(
            title = "Text To Image",
            userCredits = 1250,
            unreadNotificationCount = 0,
            onBackClick = {}
        )
    }
}

@Preview(name = "UserHeaderBar - No Credits")
@Composable
private fun UserHeaderBarNoCreditsPreview() {
    LumoraTheme {
        UserHeaderBar(
            title = "Settings",
            userCredits = 0,
            unreadNotificationCount = 12
        )
    }
}