package com.deep.lumoraai.feature.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

@Composable
fun SubscriptionScreen(
    uiState: SubscriptionUiState,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
    onClearMessage: () -> Unit,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppToolbar(
                title = "Buy Subscription",
                action = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            SubscriptionUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SubscriptionUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(uiState.message, color = Color.White)
                }
            }
            is SubscriptionUiState.Success -> {
                SubscriptionContent(
                    uiState = uiState,
                    onSelectPlan = onSelectPlan,
                    onPurchase = onPurchase,
                    onClearMessage = onClearMessage,
                    onNavigate = onNavigate,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun SubscriptionContent(
    uiState: SubscriptionUiState.Success,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
    onClearMessage: () -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.purchaseMessage != null) {
        AlertDialog(
            onDismissRequest = onClearMessage,
            title = { Text("Subscription") },
            text = { Text(uiState.purchaseMessage) },
            confirmButton = {
                TextButton(onClick = onClearMessage) { Text("OK") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F1026), Color(0xFF070714))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161838), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Unlock Lumora Pro",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Get more credits, faster generation, and premium creative tools.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            if (uiState.isDeveloperMode) {
                Text(
                    "Developer mode active — unlimited trial",
                    color = Color(0xFFADF021),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text("Choose a plan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        uiState.plans.forEach { plan ->
            SubscriptionPlanCard(
                plan = plan,
                isSelected = plan.id == uiState.selectedPlanId,
                onClick = { onSelectPlan(plan.id) }
            )
        }

        Button(
            onClick = onPurchase,
            enabled = !uiState.isPurchasing,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
        ) {
            if (uiState.isPurchasing) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (uiState.isDeveloperMode) "Activate Plan (Dev)" else "Subscribe Now",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(
            onClick = { onNavigate(Screen.Credits.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buy credit packs instead", color = Color(0xFFCFBDFF))
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> Color(0xFFA855F7)
        plan.highlighted -> Color(0xFFA855F7).copy(alpha = 0.6f)
        else -> Color.White.copy(alpha = 0.08f)
    }
    val backgroundColor = when {
        isSelected -> Color(0xFFA855F7).copy(alpha = 0.15f)
        plan.highlighted -> Color(0xFFA855F7).copy(alpha = 0.08f)
        else -> Color(0xFF161838)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(14.dp))
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(plan.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(plan.billingPeriod, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(plan.price, color = Color(0xFFCFBDFF), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (isSelected) {
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
        plan.features.forEach { feature ->
            Text("• $feature", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        }
        if (plan.highlighted) {
            Text("BEST VALUE", color = Color(0xFFADF021), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
