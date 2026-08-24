package com.deep.lumoraai.feature.createhub

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.theme.IntroPalette
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CreateHubRoute(
    onNext: () -> Unit,
    onNavigate: (String) -> Unit,
    initialPrompt: String? = null,
    initialTab: Int = 0,
    viewModel: CreateHubViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    var showAuthDialog by remember { mutableStateOf(false) }
    val user = auth.currentUser

    // If user is not logged in, show auth dialog when trying to generate
    if (showAuthDialog && user == null) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onNavigateToAuth = { onNavigate(Screen.Auth.route) }
        )
    }

    CreateHubScreen(
        uiState = viewModel.uiState,
        initialPrompt = initialPrompt,
        initialTab = initialTab,
        onGenerateImage = { prompt, style, width, height, negativePrompt, sourceImageB64 -> 
            if (user == null) {
                showAuthDialog = true
            } else {
                viewModel.generateImage(prompt, style, width, height, negativePrompt, sourceImageB64)
                onNavigate(Screen.Queue.route)
            }
        },
        onGenerateVideo = { prompt, engine, sourceImage, motionStrength, cameraDir, duration ->
            if (user == null) {
                showAuthDialog = true
            } else {
                viewModel.generateVideo(prompt, engine, sourceImage, motionStrength, cameraDir, duration)
            }
        },
        onResetState = { viewModel.load() },
        onNext = onNext, 
        onNavigate = onNavigate
    )
}

@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login Required", color = IntroPalette.TextPrimary) },
        text = { 
            Text(
                "Please login to generate images and videos. Your credits and history will be synced across devices.",
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
