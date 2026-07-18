package com.deep.lumoraai.feature.imagetovideo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.EmptyState
import com.deep.lumoraai.core.components.ErrorState
import com.deep.lumoraai.core.components.GradientButton
import com.deep.lumoraai.core.components.Loading
import com.deep.lumoraai.feature.imagetovideo.components.ImageToVideoFeatureCard

@Composable
fun ImageToVideoScreen(
    uiState: ImageToVideoUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "Image To Video") },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "imagetovideo",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        when (uiState) {
            ImageToVideoUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            ImageToVideoUiState.Empty -> EmptyState(title = "Image To Video", message = "No fake data yet.", modifier = Modifier.padding(padding))
            is ImageToVideoUiState.Error -> ErrorState(title = "Image To Video", message = uiState.message, modifier = Modifier.padding(padding))
            is ImageToVideoUiState.Success -> ImageToVideoContent(items = uiState.items, onNext = onNext, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun ImageToVideoContent(items: List<String>, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Image To Video", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        items.forEach { item -> ImageToVideoFeatureCard(title = item, subtitle = "This card will show details of your image-to-video animations.") }
        GradientButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}