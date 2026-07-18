package com.deep.lumoraai.feature.texttoimage

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
import com.deep.lumoraai.feature.texttoimage.components.TextToImageFeatureCard

@Composable
fun TextToImageScreen(
    uiState: TextToImageUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "Text To Image") },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "texttoimage",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        when (uiState) {
            TextToImageUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            TextToImageUiState.Empty -> EmptyState(title = "Text To Image", message = "No fake data yet.", modifier = Modifier.padding(padding))
            is TextToImageUiState.Error -> ErrorState(title = "Text To Image", message = uiState.message, modifier = Modifier.padding(padding))
            is TextToImageUiState.Success -> TextToImageContent(items = uiState.items, onNext = onNext, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun TextToImageContent(items: List<String>, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Text To Image", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        items.forEach { item -> TextToImageFeatureCard(title = item, subtitle = "This card will show details of your recent text-to-image generations.") }
        GradientButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}