package com.deep.lumoraai.feature.createhub

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

import com.deep.lumoraai.core.navigation.Screen

@Composable
fun CreateHubRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    initialPrompt: String? = null,
    initialTab: Int = 0,
    viewModel: CreateHubViewModel = viewModel()
) {
    CreateHubScreen(
        uiState = viewModel.uiState,
        initialPrompt = initialPrompt,
        initialTab = initialTab,
        onGenerateImage = { prompt, style, width, height, negativePrompt, sourceImageB64 -> 
            viewModel.generateImage(prompt, style, width, height, negativePrompt, sourceImageB64) 
            onNavigate(Screen.Queue.route)
        },
        onGenerateVideo = { prompt, engine, sourceImage, motionStrength, cameraDir, duration ->
            viewModel.generateVideo(prompt, engine, sourceImage, motionStrength, cameraDir, duration)
        },
        onResetState = { viewModel.load() },
        onNext = onNext, 
        onNavigate = onNavigate
    )
}