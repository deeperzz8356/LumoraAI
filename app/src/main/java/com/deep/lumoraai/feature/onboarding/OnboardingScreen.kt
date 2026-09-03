package com.deep.lumoraai.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.LumoraIntroBackground
import com.deep.lumoraai.core.theme.IntroPalette
import androidx.compose.ui.res.stringResource

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val coroutineScope = rememberCoroutineScope()
    val currentStep = pagerState.currentPage + 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(IntroPalette.BackgroundBase)
    ) {
        LumoraIntroBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            OnboardingTopBar(onSkip = onNext)
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                StandardStepScreen(currentStep = page + 1)
            }
            
            OnboardingControls(
                currentStep = currentStep,
                isLastStep = currentStep == 5,
                onNext = {
                    if (currentStep < 5) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onNext()
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun OnboardingTopBar(onSkip: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
    ) {
        OnboardingBrandMark(modifier = Modifier.align(Alignment.TopStart))
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelMedium,
                color = IntroPalette.TextMuted
            )
        }
    }
}

@Composable
fun OnboardingBrandMark(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(width = 54.dp, height = 52.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height * 0.84f)
                close()
            }
            drawPath(path = path, color = IntroPalette.AccentLime)
        }
        Text(
            text = "ai",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun StandardStepScreen(currentStep: Int) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compactHeight = maxHeight < 620.dp
        val imageAspectRatio = if (compactHeight) 0.92f else 0.8f
        val imageTopSpacing = if (compactHeight) 12.dp else 22.dp
        val descriptionTopSpacing = if (compactHeight) 8.dp else 14.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compactHeight) 92.dp else 112.dp),
                contentAlignment = Alignment.Center
            ) {
                StepTitle(step = currentStep)
            }

            Spacer(modifier = Modifier.height(imageTopSpacing))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(imageAspectRatio)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                StepIllustration(
                    step = currentStep,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(descriptionTopSpacing))

            StepDescription(step = currentStep)
            
            Spacer(modifier = Modifier.weight(1f))
        }
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
        Text(stringResource(com.deep.lumoraai.R.string.ui_next_gen), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(stringResource(com.deep.lumoraai.R.string.ui_ai_text_to_image), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(stringResource(com.deep.lumoraai.R.string.ui_generation_tool), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = IntroPalette.SecondaryText)
    }
}

@Composable
fun StepTwoTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(stringResource(com.deep.lumoraai.R.string.ui_cinematic), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Box(
                modifier = Modifier
                    .background(IntroPalette.AccentPink, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(stringResource(com.deep.lumoraai.R.string.ui_video), fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            UnderlinedText(text = "Generation", fontSize = 24.sp)
            Text(stringResource(com.deep.lumoraai.R.string.ui_from_images), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                    .background(IntroPalette.AccentPink, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(stringResource(com.deep.lumoraai.R.string.ui_generate), fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text(stringResource(com.deep.lumoraai.R.string.ui_videos), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            UnderlinedText(text = "from Text", fontSize = 24.sp)
            Text(stringResource(com.deep.lumoraai.R.string.ui_prompts), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
            Text(stringResource(com.deep.lumoraai.R.string.ui_ai), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Box(
                modifier = Modifier
                    .background(IntroPalette.AccentPink, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(stringResource(com.deep.lumoraai.R.string.ui_powered), fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        UnderlinedText(text = "Face Swap Technology", fontSize = 24.sp)
    }
}

@Composable
fun StepFiveTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(com.deep.lumoraai.R.string.ui_welcome_to), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(com.deep.lumoraai.R.string.ui_ai_2), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(stringResource(com.deep.lumoraai.R.string.ui_background), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = IntroPalette.AccentPink)
            Text(stringResource(com.deep.lumoraai.R.string.ui_eraser), fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                color = IntroPalette.AccentLime,
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
        color = IntroPalette.TextMuted,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        lineHeight = 20.sp
    )
}

@Composable
fun StepIllustration(step: Int, modifier: Modifier = Modifier) {
    if (step == 2) {
        com.deep.lumoraai.feature.onboarding.components.BeforeAfterVideoSlider(modifier = modifier)
    } else {
        val imageRes = when (step) {
            1 -> R.drawable.onboarding_1_ill
            3 -> R.drawable.onboarding_3_ill
            4 -> R.drawable.onboarding_4_ill
            5 -> R.drawable.onboarding_5_ill
            else -> R.drawable.onboarding_1_ill
        }
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Step $step",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun OnboardingControls(
    currentStep: Int,
    isLastStep: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OnboardingIndicators(currentStep = currentStep)
        if (isLastStep) {
            OnboardingStartButton(onGetStarted = onNext)
        } else {
            OnboardingNextButton(onNext = onNext)
        }
    }
}

@Composable
fun OnboardingIndicators(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { idx ->
            val active = idx + 1 == currentStep
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (active) 24.dp else 6.dp)
                    .background(
                        color = if (active) IntroPalette.IndicatorActive else IntroPalette.TextSubtle,
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
        colors = ButtonDefaults.buttonColors(containerColor = IntroPalette.PrimaryButton),
        modifier = Modifier
            .width(82.dp)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Text(stringResource(com.deep.lumoraai.R.string.ui_next), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun OnboardingStartButton(onGetStarted: () -> Unit) {
    Button(
        onClick = onGetStarted,
        colors = ButtonDefaults.buttonColors(containerColor = IntroPalette.PrimaryButton),
        modifier = Modifier
            .width(140.dp)
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(com.deep.lumoraai.R.string.ui_let_s_start), 
            color = Color.White, 
            fontWeight = FontWeight.Bold, 
            fontSize = 15.sp, 
            maxLines = 1
        )
    }
}

