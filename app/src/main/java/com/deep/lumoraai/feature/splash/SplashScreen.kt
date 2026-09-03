package com.deep.lumoraai.feature.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.ui.theme.tokens.Spacing
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource

@Composable
fun SplashScreen(
    isReady: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isReady) {
        if (isReady) {
            delay(1400)
            onNext()
        }
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Vibrant image background
        Image(
            painter = painterResource(id = R.drawable.group_48096841),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        )
        // Subtle gradient fade at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
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
            .padding(bottom = 72.dp, start = Spacing.containerMargin, end = Spacing.containerMargin)
    ) {
        SplashLogo()
        Spacer(modifier = Modifier.height(48.dp))
        SplashTitleSection()
        Spacer(modifier = Modifier.height(64.dp))
        SplashLoader()
    }
}

@Composable
fun SplashLogo() {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = stringResource(com.deep.lumoraai.R.string.ui_lumora_ai_logo),
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(120.dp)
    )
}

@Composable
fun SplashTitleSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Lumora ",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-1).sp
        )
        Text(
            text = "AI",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = (-1).sp
        )
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
    Text(
        text = "Unleash Your Imagination.",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp
    )
}

@Composable
fun SplashLoader() {
    val loadingMessages = listOf(
        "Initializing Engine...",
        "Loading Models...",
        "Optimizing Generation..."
    )
    var currentMessageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(900)
            currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(0.65f) // Narrower for a more refined look
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        
        // Fading text animation for professional feel
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Text(
            text = loadingMessages[currentMessageIndex].uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
