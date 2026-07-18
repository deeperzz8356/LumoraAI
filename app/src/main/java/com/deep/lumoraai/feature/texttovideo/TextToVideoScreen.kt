package com.deep.lumoraai.feature.texttovideo

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
import com.deep.lumoraai.feature.texttovideo.components.TextToVideoFeatureCard

@Composable
fun TextToVideoScreen(
    uiState: TextToVideoUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "Text To Video") },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "texttovideo",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        when (uiState) {
            TextToVideoUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            TextToVideoUiState.Empty -> EmptyState(title = "Text To Video", message = "No fake data yet.", modifier = Modifier.padding(padding))
            is TextToVideoUiState.Error -> ErrorState(title = "Text To Video", message = uiState.message, modifier = Modifier.padding(padding))
            is TextToVideoUiState.Success -> TextToVideoContent(items = uiState.items, onNext = onNext, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun TextToVideoContent(items: List<String>, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Text To Video", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        items.forEach { item -> TextToVideoFeatureCard(title = item, subtitle = "This card will show details of your text-to-video scenes.") }
        GradientButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}