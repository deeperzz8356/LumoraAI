package com.deep.lumoraai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource

/**
 * App Toolbar with notification bell icon and unread count badge
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    unreadCount: Int = 0,
    onNotificationClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier
            .statusBarsPadding()
            .height(64.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0B1426),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        windowInsets = WindowInsets(0.dp),
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(com.deep.lumoraai.R.string.ui_back),
                        tint = Color.White
                    )
                }
            }
        },
        actions = {
            action?.invoke()
            if (onNotificationClick != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.055f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onNotificationClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = stringResource(com.deep.lumoraai.R.string.ui_notifications),
                        modifier = Modifier
                            .size(21.dp),
                        tint = Color.White
                    )

                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }
        }
    )
}
