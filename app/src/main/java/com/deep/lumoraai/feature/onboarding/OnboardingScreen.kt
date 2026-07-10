package com.deep.lumoraai.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090909))
    ) {
        OnboardingBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            OnboardingTopBar(currentStep = currentStep, onSkip = onNext)
            OnboardingBody(
                currentStep = currentStep,
                onStepChange = { currentStep = it },
                onGetStarted = onNext
            )
        }
    }
}

@Composable
fun OnboardingBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridWidth = 40.dp.toPx()
            val gridHeight = 40.dp.toPx()
            val lineColor = Color(0xFFC2C5DF).copy(alpha = 0.05f)
            var x = 0f
            while (x < size.width) {
                drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), 0.5.dp.toPx())
                x += gridWidth
            }
            var y = 0f
            while (y < size.height) {
                drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 0.5.dp.toPx())
                y += gridHeight
            }
        }
    }
}

@Composable
fun OnboardingTopBar(currentStep: Int, onSkip: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep == 1 || currentStep == 5) {
            OnboardingLogo()
        } else {
            Spacer(modifier = Modifier.size(1.dp))
        }
        if (currentStep < 5) {
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(1.dp))
        }
    }
}

@Composable
fun OnboardingLogo() {
    Box(modifier = Modifier.size(width = 36.dp, height = 48.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.8f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = Color(0xFFD4FF3B))
        }
        Text(
            text = "ai",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 6.dp)
        )
    }
}

@Composable
fun OnboardingBody(
    currentStep: Int,
    onStepChange: (Int) -> Unit,
    onGetStarted: () -> Unit
) {
    when (currentStep) {
        1 -> StepOneScreen(onNext = { onStepChange(2) })
        2 -> StepTwoScreen(onNext = { onStepChange(3) })
        3 -> StepThreeScreen(onNext = { onStepChange(4) })
        4 -> StepFourScreen(onNext = { onStepChange(5) })
        5 -> StepFiveScreen(onGetStarted = onGetStarted)
    }
}

@Composable
fun StepOneScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepTitle(step = 1)
        Spacer(modifier = Modifier.height(16.dp))
        StepDescription(step = 1)
        Spacer(modifier = Modifier.height(24.dp))
        OnboardingControls(currentStep = 1, onNext = onNext)
        Spacer(modifier = Modifier.weight(1f))
        StepIllustration(
            step = 1,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
    }
}

@Composable
fun StepTwoScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepTitle(step = 2)
        Spacer(modifier = Modifier.weight(1f))
        StepIllustration(
            step = 2,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        StepDescription(step = 2)
        Spacer(modifier = Modifier.height(24.dp))
        OnboardingControls(currentStep = 2, onNext = onNext)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StepThreeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepTitle(step = 3)
        Spacer(modifier = Modifier.height(16.dp))
        StepDescription(step = 3)
        Spacer(modifier = Modifier.height(24.dp))
        OnboardingControls(currentStep = 3, onNext = onNext)
        Spacer(modifier = Modifier.weight(1f))
        StepIllustration(
            step = 3,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

@Composable
fun StepFourScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepTitle(step = 4)
        Spacer(modifier = Modifier.weight(1f))
        StepIllustration(
            step = 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        StepDescription(step = 4)
        Spacer(modifier = Modifier.height(24.dp))
        OnboardingControls(currentStep = 4, onNext = onNext)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StepFiveScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepTitle(step = 5)
        Spacer(modifier = Modifier.height(16.dp))
        StepDescription(step = 5)
        Spacer(modifier = Modifier.height(16.dp))
        StepIllustration(
            step = 5,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        OnboardingFinalControls(onGetStarted = onGetStarted)
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StepTitle(step: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (step) {
            1 -> StepOneTitle()
            2 -> StepTwoTitle()
            3 -> StepThreeTitle()
            4 -> StepFourTitle()
            5 -> StepFiveTitle()
        }
    }
}

@Composable
fun StepOneTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Next-Gen", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text("✦", color = Color(0xFFD4FF3B), fontSize = 22.sp)
        }
        Text("AI Text to Image", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✦", color = Color(0xFFD4FF3B), fontSize = 22.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Generation Tool", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC5C0FF))
        }
    }
}

@Composable
fun StepTwoTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Cinematic ", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Box(
                modifier = Modifier
                    .background(Color(0xFFE539B4), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Video", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            UnderlinedText(text = "Generation", fontSize = 28.sp)
            Text(" from Images", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun StepThreeTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFE539B4), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Generate", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(" Videos", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            UnderlinedText(text = "from Text", fontSize = 28.sp)
            Text(" Prompts", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun StepFourTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("AI-", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Box(
                modifier = Modifier
                    .background(Color(0xFFE539B4), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Powered", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        UnderlinedText(text = "Face Swap Technology", fontSize = 28.sp)
    }
}

@Composable
fun StepFiveTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Welcome to", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text("✦", color = Color(0xFFD4FF3B), fontSize = 22.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✦", color = Color(0xFFD4FF3B), fontSize = 22.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text("AI- ", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Background", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE539B4))
            Text(" Eraser", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun UnderlinedText(text: String, fontSize: androidx.compose.ui.unit.TextUnit, color: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
        Canvas(modifier = Modifier.width(100.dp).height(6.dp).padding(top = 2.dp)) {
            val path = Path().apply {
                moveTo(0f, 2f)
                quadraticTo(size.width / 2, size.height, size.width, 2f)
            }
            drawPath(
                path = path,
                color = Color(0xFFD4FF3B),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun StepDescription(step: Int, modifier: Modifier = Modifier) {
    val text = when (step) {
        1 -> "Create stunning, high-resolution images with hyper realistic details directly from your text prompts using Imagine V5."
        2 -> "Bring your static images to life by transforming them into high-quality cinematic videos using advanced AI video models."
        3 -> "Generate high-quality video clips directly from descriptive text prompts in just a few clicks."
        4 -> "Easily switch faces in your photos using our AI Face Swipe feature. Perfect for creating amusing images or trying different looks."
        5 -> "Effortlessly remove backgrounds from any image with AI precision and generate clean cutouts in seconds."
        else -> ""
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        lineHeight = 20.sp
    )
}

@Composable
fun StepIllustration(step: Int, modifier: Modifier = Modifier) {
    val imageRes = when (step) {
        1 -> R.drawable.onboarding_1_ill
        2 -> R.drawable.onboarding_2_ill
        3 -> R.drawable.onboarding_3_ill
        4 -> R.drawable.onboarding_4_ill
        5 -> R.drawable.onboarding_5_ill
        else -> R.drawable.onboarding_1_ill
    }
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Step $step",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun OnboardingControls(
    currentStep: Int,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        OnboardingIndicators(currentStep = currentStep)
        OnboardingNextButton(onNext = onNext)
    }
}

@Composable
fun OnboardingIndicators(currentStep: Int) {
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
                        color = if (active) Color(0xFFCFBDFF) else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
fun OnboardingNextButton(onNext: () -> Unit) {
    Button(
        onClick = onNext,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7E50EF),
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = "Next",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OnboardingFinalControls(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        OnboardingStartButton(onGetStarted = onGetStarted)
        Spacer(modifier = Modifier.height(8.dp))
        OnboardingUtilityButtons()
    }
}

@Composable
fun OnboardingStartButton(onGetStarted: () -> Unit) {
    Button(
        onClick = onGetStarted,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E50EF)),
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Let's Start", color = Color.White, fontWeight = FontWeight.Bold)
            OnboardingStartArrow()
        }
    }
}

@Composable
fun OnboardingStartArrow() {
    Box(
        modifier = Modifier.size(40.dp).background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(">>>", color = Color(0xFF7E50EF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun OnboardingUtilityButtons() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingSmallButton(text = "Rate Us", icon = "⭐", modifier = Modifier.weight(1f))
            OnboardingSmallButton(text = "Share App", icon = "🔗", modifier = Modifier.weight(1f))
        }
        OnboardingSmallButton(text = "Privacy Policy", icon = "🛡️", modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun OnboardingSmallButton(text: String, icon: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(Color(0xFF131524), RoundedCornerShape(24.dp))
            .border(1.dp, Color(0xFF2C2F48), RoundedCornerShape(24.dp))
            .clickable { /* Action */ }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(text = text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}