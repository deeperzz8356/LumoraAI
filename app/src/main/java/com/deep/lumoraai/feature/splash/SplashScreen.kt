package com.deep.lumoraai.feature.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(3000)
        onNext()
    }
    SplashBackground(modifier = modifier) {
        SplashBottomContent()
    }
}

@Composable
fun SplashBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161B40),
                        Color(0xFF0D0F24)
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.group_48096841),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(top = 32.dp)
        )
        content()
    }
}

@Composable
fun SplashBottomContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 64.dp, start = 32.dp, end = 32.dp)
    ) {
        SplashTitleSection()
        Spacer(modifier = Modifier.height(64.dp))
        CircularDottedLoader()
    }
}

@Composable
fun SplashTitleSection() {
    Text(
        text = "✨",
        fontSize = 24.sp,
        color = Color(0xFFFFD700),
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        text = "Lumora AI",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        fontSize = 36.sp
    )
    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(4.dp)
            .padding(top = 4.dp, bottom = 24.dp)
            .background(Color(0xFF7E50EF))
    )
    Text(
        text = "Unleash Your Imagination with AI-Generated Masterpieces!",
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.9f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun CircularDottedLoader() {
    val transition = rememberInfiniteTransition(label = "loader")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    Canvas(
        modifier = Modifier
            .size(48.dp)
            .rotate(rotation)
    ) {
        val dotCount = 8
        val radius = size.width / 2
        val dotRadius = 4.dp.toPx()
        for (i in 0 until dotCount) {
            val angle = (i * (360f / dotCount)) * (PI / 180f)
            val alpha = 1f - (i.toFloat() / dotCount)
            val x = center.x + (radius - dotRadius) * cos(angle).toFloat()
            val y = center.y + (radius - dotRadius) * sin(angle).toFloat()
            drawCircle(Color.White, dotRadius, Offset(x, y), alpha)
        }
    }
}