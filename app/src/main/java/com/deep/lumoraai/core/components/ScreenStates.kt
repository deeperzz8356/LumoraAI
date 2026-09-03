package com.deep.lumoraai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.ui.theme.tokens.Spacing

@Composable
fun AppLoadingScreen(modifier: Modifier = Modifier) {
    val lime = Color(0xFFD6FF2F)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF081020)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .background(Color(0xFF10192D), CircleShape)
                    .border(1.dp, lime.copy(alpha = 0.26f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = lime,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(54.dp)
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(lime.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = lime,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Text(
                text = "Loading",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Preparing your workspace...",
                color = Color(0xFF94A0B8),
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun AppEmptyScreen(
    title: String, 
    body: String, 
    modifier: Modifier = Modifier,
    illustration: (@Composable () -> Unit)? = null, 
    action: Pair<String, () -> Unit>? = null
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        illustration?.invoke()
        Spacer(Modifier.height(Spacing.lg))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            body, 
            style = MaterialTheme.typography.bodyMedium, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) { 
            Spacer(Modifier.height(Spacing.lg))
            AppButton(text = action.first, onClick = action.second) 
        }
    }
}

@Composable
fun AppErrorScreen(
    message: String, 
    onRetry: (() -> Unit)? = null, 
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Info, contentDescription = null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(Spacing.md))
        Text("Something went wrong", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            message, 
            style = MaterialTheme.typography.bodyMedium, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (onRetry != null) { 
            Spacer(Modifier.height(Spacing.lg))
            AppButton("Try again", onRetry, variant = AppButtonVariant.Tonal) 
        }
    }
}
