package com.deep.lumoraai.feature.network

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.LumoraIntroBackground
import com.deep.lumoraai.core.components.LumoraIntroPrimaryButton
import com.deep.lumoraai.core.components.LumoraIntroSecondaryButton
import com.deep.lumoraai.core.theme.IntroPalette

@Composable
fun NoInternetScreen(
    onTurnOnNetwork: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IntroPalette.BackgroundBase)
    ) {
        LumoraIntroBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WifiStatusArt()
            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = "No internet connection",
                color = IntroPalette.TextPrimary,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Please turn on Wi-Fi or mobile data to continue using Lumora AI.",
                color = IntroPalette.TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(34.dp))
            LumoraIntroPrimaryButton(
                text = "Turn on network",
                onClick = onTurnOnNetwork,
                leadingIcon = Icons.Default.Wifi,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            LumoraIntroSecondaryButton(
                text = "Retry",
                onClick = onRetry,
                leadingIcon = Icons.Default.Refresh,
                height = 52.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WifiStatusArt() {
    Box(
        modifier = Modifier
            .size(142.dp)
            .shadow(28.dp, CircleShape, clip = false)
            .background(IntroPalette.SurfaceRaised, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = IntroPalette.PrimaryButton.copy(alpha = 0.24f),
                radius = size.minDimension * 0.45f
            )
            drawCircle(
                color = IntroPalette.AccentLime.copy(alpha = 0.18f),
                radius = size.minDimension * 0.32f,
                center = Offset(size.width * 0.68f, size.height * 0.32f)
            )
            drawCircle(
                color = IntroPalette.BorderSubtle,
                radius = size.minDimension * 0.48f,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Icon(
            imageVector = Icons.Default.SignalWifiOff,
            contentDescription = null,
            tint = IntroPalette.TextPrimary,
            modifier = Modifier.size(58.dp)
        )
    }
}
