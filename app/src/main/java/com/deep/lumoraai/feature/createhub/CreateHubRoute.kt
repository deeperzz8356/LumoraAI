package com.deep.lumoraai.feature.createhub

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate
import com.deep.lumoraai.core.theme.IntroPalette
import com.deep.lumoraai.core.utils.GuestIdentity

@Composable
fun CreateHubRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit = onNext,
    initialPrompt: String? = null,
    initialTab: Int = 0,
    viewModel: CreateHubViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState

    LaunchedEffect(uiState) {
        when (uiState) {
            CreateHubUiState.Generating -> onNavigate(Screen.Queue.route)
            CreateHubUiState.TrialExpired -> {
                GuestIdentity.markTrialExhausted(context)
                onNavigate(Screen.Auth.route)
                viewModel.load()
            }
            else -> Unit
        }
    }

    if (uiState is CreateHubUiState.TrialExpired) {
        AuthDialog(
            title = "Free trial finished",
            message = GenerationGate.insufficientCreditsMessage(),
            onDismiss = { viewModel.load() },
            onNavigateToAuth = { onNavigate(Screen.Auth.route) }
        )
    }

    CreateHubScreen(
        uiState = uiState,
        initialPrompt = initialPrompt,
        initialTab = initialTab,
        onGenerateImage = { prompt, style, width, height, negativePrompt, sourceImageB64 -> 
            viewModel.generateImage(prompt, style, width, height, negativePrompt, sourceImageB64)
        },
        onGenerateVideo = { prompt, engine, sourceImage, motionStrength, cameraDir, duration ->
            viewModel.generateVideo(prompt, engine, sourceImage, motionStrength, cameraDir, duration)
        },
        onResetState = { viewModel.load() },
        onNext = onNext, 
        onNavigate = onNavigate,
        onBack = onBack
    )
}

@Composable
fun AuthDialog(
    title: String = "Login Required",
    message: String = "Please login to generate images and videos. Your credits and history will be synced across devices.",
    onDismiss: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = IntroPalette.TextPrimary) },
        text = { 
            Text(
                message,
                color = IntroPalette.TextMuted
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    onNavigateToAuth()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = IntroPalette.PrimaryButton
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Login", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text("Cancel", color = IntroPalette.TextMuted)
            }
        },
        containerColor = IntroPalette.SurfaceRaised,
        titleContentColor = IntroPalette.TextPrimary,
        textContentColor = IntroPalette.TextMuted
    )
}
