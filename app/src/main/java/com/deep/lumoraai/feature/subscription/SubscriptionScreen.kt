package com.deep.lumoraai.feature.subscription

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan

// Theme colors matching Home/Profile pages
private val SubBackground = Color(0xFF081020)
private val SubCard = Color(0xFF10192D)
private val SubStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

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
        containerColor = SubBackground,
        topBar = {
            AppToolbar(
                title = "Buy Subscription",
                action = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
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
                    CircularProgressIndicator(color = Lime)
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
            containerColor = SubCard,
            title = { Text("Subscription", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(uiState.purchaseMessage, color = Color.White) },
            confirmButton = {
                TextButton(onClick = onClearMessage) { Text("OK", color = Lime) }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SubBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = SubCard,
            border = BorderStroke(1.dp, SubStroke.copy(alpha = 0.72f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Unlock Lumora Pro",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Get more credits, faster generation, and premium creative tools.",
                    color = Muted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                if (uiState.isDeveloperMode) {
                    Text(
                        "Developer mode active — unlimited trial",
                        color = Lime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text("Choose a plan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)

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
            shape = CardShape,
            colors = ButtonDefaults.buttonColors(containerColor = Lime)
        ) {
            if (uiState.isPurchasing) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = SubBackground,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (uiState.isDeveloperMode) "Activate Plan (Dev)" else "Subscribe Now",
                    color = SubBackground,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }

        TextButton(
            onClick = { onNavigate(Screen.Credits.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buy credit packs instead", color = Lime, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isSelected -> Lime
        plan.highlighted -> Lime.copy(alpha = 0.6f)
        else -> SubStroke
    }
    val accentColor = when {
        isSelected -> Lime
        plan.highlighted -> Purple
        else -> Muted
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = SubCard,
        border = BorderStroke(1.dp, borderColor.copy(alpha = if (isSelected) 0.72f else 0.58f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(plan.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(plan.billingPeriod, color = Muted, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(plan.price, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    if (isSelected) {
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        Icon(Icons.Default.Check, contentDescription = null, tint = Lime, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            plan.features.forEach { feature ->
                Text("• $feature", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }
            if (plan.highlighted) {
                Text("BEST VALUE", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
