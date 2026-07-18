package com.deep.lumoraai.feature.history

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
import com.deep.lumoraai.feature.history.components.HistoryFeatureCard

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "History") },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "history",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        when (uiState) {
            HistoryUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            HistoryUiState.Empty -> EmptyState(title = "History", message = "No fake data yet.", modifier = Modifier.padding(padding))
            is HistoryUiState.Error -> ErrorState(title = "History", message = uiState.message, modifier = Modifier.padding(padding))
            is HistoryUiState.Success -> HistoryContent(items = uiState.items, onNext = onNext, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun HistoryContent(items: List<String>, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("History", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        items.forEach { item -> HistoryFeatureCard(title = item, subtitle = "This card displays your past creations stored in the database.") }
        GradientButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}