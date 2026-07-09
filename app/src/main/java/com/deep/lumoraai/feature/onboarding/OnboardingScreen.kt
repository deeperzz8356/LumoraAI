package com.deep.lumoraai.feature.onboarding

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090909))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF161A2D).copy(alpha = 0.4f),
                            Color(0xFF121212).copy(alpha = 0.2f),
                            Color(0xFF090909)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Canvas(modifier = Modifier.size(24.dp)) {
                        drawCircle(color = Color(0xFFC2C5DF), radius = size.width * 0.4f)
                    }
                    Text(
                        text = "Lumora AI",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.02).em
                    )
                }

                if (currentStep < 4) {
                    TextButton(onClick = onNext) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = currentStep, label = "illustration") { step ->
                        when (step) {
                            1 -> StepOneIllustration()
                            2 -> StepTwoIllustration()
                            3 -> StepThreeIllustration()
                            else -> StepFourIllustration()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Crossfade(targetState = currentStep, label = "textTitle") { step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (step) {
                                1 -> "Enhance, Restore & Perfect"
                                2 -> "Create Anything You Imagine"
                                3 -> "Still Image to Cinematic Video"
                                else -> "Join a Creative Community"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.02).em
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (step) {
                                1 -> "Remove backgrounds, upscale resolution, and restore old photos with one-tap precision."
                                2 -> "Type prompts and watch Lumora transform your words into gorgeous high-fidelity art."
                                3 -> "Animate static visual layers to bring high-end cinematic sequences to life."
                                else -> "Share your renders, remix other prompts, and get inspired by creators worldwide."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { idx ->
                        val active = idx + 1 == currentStep
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (active) 24.dp else 6.dp)
                                .background(
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < 4) {
                            currentStep++
                        } else {
                            onNext()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = if (currentStep < 4) "Next" else "Get Started",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (currentStep == 4) {
                    Text(
                        text = "Already have an account? Sign in",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onNext() }
                            .padding(vertical = 4.dp)
                    )
                }

                Text(
                    text = "v1.0.4 Premium Studio Edition",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}


@Composable
private fun StepOneIllustration() {
    var isDragging by remember { mutableStateOf(false) }
    var sliderFraction by remember { mutableStateOf(0.5f) }

    if (!isDragging) {
        val infiniteTransition = rememberInfiniteTransition(label = "slider")
        val animatedFraction by infiniteTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.55f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "fraction"
        )
        sliderFraction = animatedFraction
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color(0xFF0E0E0E), RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                ) { change, _ ->
                    sliderFraction = (change.position.x / size.width).coerceIn(0f, 1f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    sliderFraction = (offset.x / size.width).coerceIn(0f, 1f)
                }
            }
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFFC857), Color(0xFFFF7A90))),
                    radius = size.height * 0.3f,
                    center = Offset(size.width * 0.5f, size.height * 0.4f)
                )

                val path1 = Path().apply {
                    moveTo(size.width * 0.3f, size.height)
                    lineTo(size.width * 0.7f, size.height * 0.3f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(
                    path = path1,
                    brush = Brush.verticalGradient(listOf(Color(0xFFC0C1FF), Color(0xFF161A2D)))
                )

                val path2 = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width * 0.4f, size.height * 0.45f)
                    lineTo(size.width * 0.8f, size.height)
                    close()
                }
                drawPath(
                    path = path2,
                    brush = Brush.verticalGradient(listOf(Color(0xFFB8C4FF), Color(0xFF131313)))
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(sliderFraction)
                .clipToBounds()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(Color(0xFF3E352F))

                drawCircle(
                    color = Color(0xFF8B7355),
                    radius = size.height * 0.3f,
                    center = Offset(size.width * 0.5f, size.height * 0.4f)
                )

                val path1 = Path().apply {
                    moveTo(size.width * 0.3f, size.height)
                    lineTo(size.width * 0.7f, size.height * 0.3f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(path = path1, color = Color(0xFF5C5047))

                val path2 = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width * 0.4f, size.height * 0.45f)
                    lineTo(size.width * 0.8f, size.height)
                    close()
                }
                drawPath(path = path2, color = Color(0xFF4A3E37))

                repeat(6) { index ->
                    drawLine(
                        color = Color(0xFF2C221C),
                        start = Offset(size.width * (0.15f * index + 0.05f), 0f),
                        end = Offset(size.width * (0.15f * index + 0.1f), size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        val dividerX = (this@BoxWithConstraints.maxWidth * sliderFraction)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(Color(0xFFC2C5DF))
                .align(Alignment.TopStart)
                .padding(start = dividerX - 1.dp)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterStart)
                .padding(start = dividerX - 18.dp)
                .background(Color(0xFF131313), CircleShape)
                .border(2.dp, Color(0xFFC2C5DF), CircleShape)
        ) {
            Text(
                text = "⇄",
                color = Color(0xFFC2C5DF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "BEFORE",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Text(
            text = "AFTER",
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StepTwoIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color(0xFF0E0E0E), RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF161A2D), Color(0xFF090909)))
            )

            val heights = listOf(0.7f, 0.45f, 0.8f, 0.6f, 0.5f, 0.75f)
            val count = heights.size
            val buildingWidth = size.width / (count + 1)
            heights.forEachIndexed { idx, heightFraction ->
                val h = size.height * heightFraction
                val x = (idx + 0.5f) * buildingWidth
                val y = size.height - h

                drawRect(
                    color = Color(0xFF121212),
                    topLeft = Offset(x, y),
                    size = Size(buildingWidth - 8.dp.toPx(), h)
                )

                drawRect(
                    color = Color(0xFFC0C1FF).copy(alpha = 0.2f),
                    topLeft = Offset(x, y),
                    size = Size(buildingWidth - 8.dp.toPx(), h),
                    style = Stroke(width = 1.dp.toPx())
                )

                var winY = y + 16.dp.toPx()
                while (winY < size.height - 16.dp.toPx()) {
                    drawRect(
                        color = Color(0xFFFFC857).copy(alpha = 0.3f),
                        topLeft = Offset(x + (buildingWidth / 4), winY),
                        size = Size(4.dp.toPx(), 4.dp.toPx())
                    )
                    drawRect(
                        color = Color(0xFFC2C5DF).copy(alpha = 0.3f),
                        topLeft = Offset(x + (buildingWidth * 3 / 4) - 4.dp.toPx(), winY),
                        size = Size(4.dp.toPx(), 4.dp.toPx())
                    )
                    winY += 20.dp.toPx()
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .width(260.dp)
                .background(
                    Color(0xFF131313).copy(alpha = 0.9f),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    Color(0xFFC2C5DF).copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(color = Color(0xFFC2C5DF))
                    }
                    Text(
                        text = "\"A neon cityscape at dawn...\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.66f)
                            .background(
                                Color(0xFFC2C5DF),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun StepThreeIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color(0xFF0E0E0E), RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF131313), Color(0xFF161A2D))
                )
            )

            val linesCount = 8
            val gap = size.width / linesCount
            repeat(linesCount) { index ->
                drawLine(
                    color = Color(0xFFC2C5DF).copy(alpha = 0.05f),
                    start = Offset(index * gap, 0f),
                    end = Offset(index * gap, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF131313).copy(alpha = 0.7f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFFF6B6B), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFFFFC857), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF63D297), CircleShape)
                        )
                    }
                    Text(
                        text = "Cinema_01.mp4",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(modifier = Modifier.size(6.dp))
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFF131313).copy(alpha = 0.7f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "00:04:12",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "00:15:00",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(1.5.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.35f)
                                .background(Color(0xFFC2C5DF), RoundedCornerShape(1.5.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepFourIllustration() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(Color(0xFF090909), RoundedCornerShape(12.dp))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .fillMaxWidth(0.48f)
                    .align(Alignment.TopStart)
                    .rotate(-4f)
                    .background(Color(0xFF121212), RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        Color(0xFFC2C5DF).copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFC0C1FF).copy(alpha = 0.2f),
                                Color(0xFF161A2D).copy(alpha = 0.4f)
                            )
                        )
                    )
                    drawCircle(
                        color = Color(0xFFB8C4FF).copy(alpha = 0.3f),
                        radius = size.width * 0.2f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nova_Flux",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "♥ 1.2k",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight(0.6f)
                    .fillMaxWidth(0.45f)
                    .align(Alignment.TopEnd)
                    .rotate(6f)
                    .background(Color(0xFF121212), RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        Color(0xFFC2C5DF).copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFFFC857).copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, size.height)
                            cubicTo(
                                size.width * 0.3f,
                                size.height * 0.2f,
                                size.width * 0.7f,
                                size.height * 0.8f,
                                size.width,
                                0f
                            )
                            lineTo(size.width, size.height)
                            close()
                        },
                        color = Color(0xFFFF7A90).copy(alpha = 0.2f)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            0.5.dp,
                            Color(0xFFC2C5DF).copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Remix",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight(0.45f)
                    .fillMaxWidth(0.42f)
                    .align(Alignment.BottomEnd)
                    .rotate(-2f)
                    .background(Color(0xFF121212), RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        Color(0xFFC2C5DF).copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFFC2C5DF).copy(alpha = 0.1f),
                        radius = size.width * 0.3f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f)
                    )
                    drawCircle(
                        color = Color(0xFFC2C5DF).copy(alpha = 0.2f),
                        radius = size.width * 0.2f,
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}