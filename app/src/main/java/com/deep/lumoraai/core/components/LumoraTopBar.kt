package com.deep.lumoraai.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.restrictions.GenerationGate
import androidx.compose.ui.res.stringResource

private val Lime = Color(0xFFD6FF2F)

@Composable
fun LumoraTopBar(
    credits: Int,
    onProfileClick: () -> Unit,
    onCreditsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "LUMORIA AI",
    hasUnreadNotifications: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF2D77FF), CircleShape)
                    .clickable(onClick = onProfileClick)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_avatar),
                    contentDescription = stringResource(com.deep.lumoraai.R.string.ui_profile),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LumoraCreditsChip(credits = credits, onClick = onCreditsClick)
            LumoraNotificationBell(
                hasUnreadNotifications = hasUnreadNotifications,
                onClick = onNotificationsClick
            )
        }
    }
}

@Composable
fun LumoraNotificationBell(
    hasUnreadNotifications: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.055f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = stringResource(com.deep.lumoraai.R.string.ui_notifications),
            tint = Color(0xFFDFF7F4),
            modifier = Modifier.size(21.dp)
        )
        if (hasUnreadNotifications) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .align(Alignment.TopEnd)
                    .background(Lime, CircleShape)
            )
        }
    }
}

@Composable
fun LumoraCreditsChip(
    credits: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = if (credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) {
        "Unlimited"
    } else {
        credits.toString()
    }

    Row(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("✦", color = Lime, fontSize = 14.sp, lineHeight = 14.sp)
        Text(
            text = label,
            color = Lime,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
