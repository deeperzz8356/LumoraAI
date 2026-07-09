package com.deep.lumoraai.feature.createhub

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
import com.deep.lumoraai.feature.createhub.components.CreateHubFeatureCard

@Composable
fun CreateHubScreen(
    uiState: CreateHubUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "Create Hub") },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "createhub",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        when (uiState) {
            CreateHubUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            CreateHubUiState.Empty -> EmptyState(title = "Create Hub", message = "No fake data yet.", modifier = Modifier.padding(padding))
            is CreateHubUiState.Error -> ErrorState(title = "Create Hub", message = uiState.message, modifier = Modifier.padding(padding))
            is CreateHubUiState.Success -> CreateHubContent(items = uiState.items, onNext = onNext, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun CreateHubContent(items: List<String>, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Create Hub", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Architecture placeholder using fake local data.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        items.forEach { item -> CreateHubFeatureCard(title = item, subtitle = "Local fake data") }
        GradientButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}