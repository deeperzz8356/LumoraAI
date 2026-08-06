package com.deep.lumoraai.feature.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.EmptyState
import com.deep.lumoraai.core.components.ErrorState
import com.deep.lumoraai.core.components.GradientButton
import com.deep.lumoraai.core.components.Loading
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

@Composable
fun SubscriptionScreen(
    uiState: SubscriptionUiState,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    onSelectPlan: (String) -> Unit = {},
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
            SubscriptionUiState.Empty -> EmptyState(title = "Subscription", message = "No plans are available right now.", modifier = Modifier.padding(padding))
            is SubscriptionUiState.Error -> ErrorState(title = "Subscription", message = uiState.message, modifier = Modifier.padding(padding))
            is SubscriptionUiState.Success -> SubscriptionContent(
                plans = uiState.plans,
                currentPlan = uiState.currentPlan,
                onSelectPlan = onSelectPlan,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun SubscriptionContent(
    plans: List<SubscriptionPlan>,
    currentPlan: String?,
    onSelectPlan: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Choose your plan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Unlock more credits and video generations.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
        }

        items(plans) { plan ->
            PlanCard(plan = plan, isCurrent = plan.code == currentPlan, onSelectPlan = onSelectPlan)
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isCurrent: Boolean,
    onSelectPlan: (String) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = if (plan.isPopular) 4.dp else 2.dp,
        color = if (plan.isPopular) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${plan.priceUsd.toInt()} USD / month", style = MaterialTheme.typography.bodyMedium)
                }
                if (plan.isPopular) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text("Popular", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Text("${plan.monthlyCredits} image credits • ${plan.videoCredits} video credits", style = MaterialTheme.typography.bodyMedium)
            plan.features.forEach { feature ->
                Text("• $feature", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (plan.signupBonusCredits > 0) {
                Text("Signup bonus: ${plan.signupBonusCredits} free credit", style = MaterialTheme.typography.bodySmall)
            }

            GradientButton(
                text = if (isCurrent) "Current plan" else if (plan.code == "free") "Continue with Free" else "Upgrade",
                onClick = { onSelectPlan(plan.code) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}