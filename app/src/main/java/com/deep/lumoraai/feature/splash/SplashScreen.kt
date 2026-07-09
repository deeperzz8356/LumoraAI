package com.deep.lumoraai.feature.splash

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableStateOf(0.05f) }
    var loadingText by remember { mutableStateOf("Initializing Experience...") }

    val steps = listOf(
        Pair(0.15f, "Connecting to Neural Core..."),
        Pair(0.45f, "Optimizing Luminous Engine..."),
        Pair(0.72f, "Synchronizing Studio Assets..."),
        Pair(0.90f, "Finalizing Interface..."),
        Pair(1.00f, "Ready")
    )

    LaunchedEffect(Unit) {
        delay(1000)
        for (step in steps) {
            progress = step.first
            loadingText = step.second
            if (step.first == 1.00f) {
                delay(800)
                onNext()
            } else {
                delay(kotlin.random.Random.nextLong(800, 2300))
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF161A2D),
                        Color(0xFF121212),
                        Color(0xFF090909)
                    )
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.04f)
        ) {
            val patternSize = 60.dp.toPx()
            val strokeWidth = 0.5.dp.toPx()
            val circleRadius = 1.dp.toPx()
            val width = size.width
            val height = size.height
            var x = 0f
            while (x < width) {
                var y = 0f
                while (y < height) {
                    drawCircle(
                        color = Color(0xFFC2C5DF),
                        radius = circleRadius,
                        center = Offset(x + 2.dp.toPx(), y + 2.dp.toPx()),
                        alpha = 0.5f
                    )
                    drawLine(
                        color = Color(0xFFC2C5DF),
                        start = Offset(x + 2.dp.toPx(), y + 2.dp.toPx()),
                        end = Offset(x + 58.dp.toPx(), y + 58.dp.toPx()),
                        strokeWidth = strokeWidth,
                        alpha = 0.2f
                    )
                    y += patternSize
                }
                x += patternSize
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "glow")
                val glowScale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowScale"
                )
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glowAlpha"
                )
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(glowScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFC2C5DF).copy(alpha = glowAlpha), Color.Transparent)
                            )
                        )
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    BlurOnIcon(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lumora AI",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.02).em
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CREATE BEYOND IMAGINATION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                letterSpacing = 0.2.em
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(280.dp)
                    .padding(bottom = 96.dp)
            ) {
                Crossfade(targetState = loadingText, label = "loadingText") { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.02.em
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(1000, easing = EaseOut),
                    label = "progress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(2.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .alpha(0.4f)
            ) {
                Text(
                    text = "v1.0.0 (build 2026.1)",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "PREMIUM STUDIO EDITION",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.1.em
                )
            }
        }
    }
}

@Composable
private fun BlurOnIcon(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Canvas(modifier = modifier) {
        val count = 5
        val cellSize = size.width / (count + 1)
        for (r in 0 until count) {
            for (c in 0 until count) {
                val dx = r - 2f
                val dy = c - 2f
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                val radius = when {
                    dist == 0f -> size.width * 0.08f
                    dist <= 1f -> size.width * 0.06f
                    dist <= 1.5f -> size.width * 0.04f
                    dist <= 2f -> size.width * 0.03f
                    else -> size.width * 0.02f
                }
                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset((r + 1) * cellSize, (c + 1) * cellSize)
                )
            }
        }
    }
}