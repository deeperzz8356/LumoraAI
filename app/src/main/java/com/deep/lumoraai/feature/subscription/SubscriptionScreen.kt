package com.deep.lumoraai.feature.subscription

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
import com.deep.lumoraai.feature.subscription.components.SubscriptionFeatureCard

@Composable
fun SubscriptionScreen(
    uiState: SubscriptionUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppToolbar(title = "Subscription") },
        bottomBar = {
            BottomNavigationBar(
                items = listOf("home", "createhub", "queue", "profile"),
                selected = "subscription",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        when (uiState) {
            SubscriptionUiState.Loading -> Loading(modifier = Modifier.padding(padding))
            SubscriptionUiState.Empty -> EmptyState(title = "Subscription", message = "No fake data yet.", modifier = Modifier.padding(padding))
            is SubscriptionUiState.Error -> ErrorState(title = "Subscription", message = uiState.message, modifier = Modifier.padding(padding))
            is SubscriptionUiState.Success -> SubscriptionContent(items = uiState.items, onNext = onNext, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun SubscriptionContent(items: List<String>, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Subscription", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        items.forEach { item -> SubscriptionFeatureCard(title = item, subtitle = "This option displays details of your current membership level and features.") }
        GradientButton(text = "Continue", onClick = onNext, modifier = Modifier.fillMaxWidth())
    }
}