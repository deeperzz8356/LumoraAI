package com.deep.lumoraai.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.ui.theme.tokens.Spacing
import androidx.compose.ui.res.stringResource

@Composable
fun AppLoadingScreen(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "appLoader")
    val rotation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "appLoaderRotation"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF081020)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(46.dp)) {
            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = size.minDimension / 2f - stroke.width / 2f,
                style = stroke
            )
            drawArc(
                color = Color(0xFFD6FF2F),
                startAngle = rotation.value - 90f,
                sweepAngle = 280f,
                useCenter = false,
                style = stroke
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
        Text(stringResource(com.deep.lumoraai.R.string.ui_something_went_wrong), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
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
