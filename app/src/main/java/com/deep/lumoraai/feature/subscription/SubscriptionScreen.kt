package com.deep.lumoraai.feature.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.data.billing.BillingState
import com.deep.lumoraai.feature.subscription.model.SubscriptionPlan
import androidx.compose.ui.res.stringResource

private val SubBackground = Color(0xFF081020)
private val SubCard = Color(0xFF10192D)
private val SubStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Pink = Color(0xFFFF3D9D)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun SubscriptionScreen(
    uiState: SubscriptionUiState,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    onClearMessage: () -> Unit,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SubBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SubBackground)
                .padding(padding)
        ) {
            when (uiState) {
                SubscriptionUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Lime)
                    }
                }
                is SubscriptionUiState.Error -> AppErrorScreen(message = uiState.message)
                is SubscriptionUiState.Success -> SubscriptionContent(
                    uiState = uiState,
                    onSelectPlan = onSelectPlan,
                    onPurchase = onPurchase,
                    onRestore = onRestore,
                    onClearMessage = onClearMessage,
                    onBack = onBack,
                    onNavigate = onNavigate
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
    onRestore: () -> Unit,
    onClearMessage: () -> Unit,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val selectedPlan = uiState.plans.firstOrNull { it.id == uiState.selectedPlanId }

    if (uiState.purchaseMessage != null) {
        AlertDialog(
            onDismissRequest = onClearMessage,
            containerColor = SubCard,
            title = { Text(stringResource(com.deep.lumoraai.R.string.ui_subscription), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(uiState.purchaseMessage, color = Color.White) },
            confirmButton = { TextButton(onClick = onClearMessage) { Text(stringResource(com.deep.lumoraai.R.string.ui_ok), color = Lime) } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PageTopBar(title = stringResource(com.deep.lumoraai.R.string.ui_subscription_2), subtitle = stringResource(com.deep.lumoraai.R.string.ui_upgrade_your_studio), onBack = onBack)
        ProHero(isDeveloperMode = uiState.isDeveloperMode)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FeatureTile("Faster", "priority runs", Icons.Default.FlashOn, Lime, Modifier.weight(1f))
            FeatureTile("Storage", "history sync", Icons.Default.CloudDone, Cyan, Modifier.weight(1f))
            FeatureTile("Tools", "Pro access", Icons.Default.LockOpen, Purple, Modifier.weight(1f))
        }

        Text(stringResource(com.deep.lumoraai.R.string.ui_choose_plan), color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        if (uiState.billingState is BillingState.Unavailable) {
            Text(
                "Google Play subscriptions are currently unavailable. Please try again later.",
                color = Muted,
                fontSize = 12.sp
            )
        }
        uiState.plans.forEach { plan ->
            SubscriptionPlanCard(
                plan = plan,
                isSelected = plan.id == uiState.selectedPlanId,
                onClick = { onSelectPlan(plan.id) }
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = SubCard,
            border = BorderStroke(1.dp, Lime.copy(alpha = 0.32f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(com.deep.lumoraai.R.string.ui_ready_to_create_more), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(selectedPlan?.name ?: "Select a plan", color = Muted, fontSize = 12.sp)
                    }
                    Text(selectedPlan?.price.orEmpty(), color = Lime, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                Button(
                    onClick = onPurchase,
                    enabled = !uiState.isPurchasing && selectedPlan != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Lime, disabledContainerColor = Lime.copy(alpha = 0.38f))
                ) {
                    if (uiState.isPurchasing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SubBackground, strokeWidth = 2.dp)
                    } else {
                        Text(if (uiState.isDeveloperMode) "Activate Plan (Dev)" else "Subscribe Now", color = SubBackground, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().clickable { onNavigate(Screen.Credits.route) },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(com.deep.lumoraai.R.string.ui_buy_credit_packs_instead), color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Lime, modifier = Modifier.size(16.dp))
        }

        TextButton(onClick = onRestore, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(com.deep.lumoraai.R.string.ui_restore_purchases), color = Lime)
        }

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun PageTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(com.deep.lumoraai.R.string.ui_back), tint = Color.White)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, color = Color.White, fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun ProHero(isDeveloperMode: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(184.dp),
        shape = CardShape,
        color = SubCard,
        border = BorderStroke(1.dp, Lime.copy(alpha = 0.26f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            Column(modifier = Modifier.fillMaxWidth(0.72f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(com.deep.lumoraai.R.string.ui_lumora_pro), color = Lime, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(com.deep.lumoraai.R.string.ui_create_without_limits), color = Color.White, fontSize = 29.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold)
                Text(if (isDeveloperMode) "Developer mode gives unlimited trial access." else "Credits, priority generation, and premium creative workflows.", color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Pink.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Pink, modifier = Modifier.size(31.dp))
            }
        }
    }
}

@Composable
private fun FeatureTile(title: String, subtitle: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(92.dp),
        shape = CardShape,
        color = SubCard,
        border = BorderStroke(1.dp, SubStroke.copy(alpha = 0.58f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            AccentIcon(icon, accent)
            Column {
                Text(title, color = Color.White, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = Muted, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accent = when {
        isSelected -> Lime
        plan.highlighted -> Purple
        else -> Muted
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = SubCard,
        border = BorderStroke(1.dp, if (isSelected) Lime.copy(alpha = 0.82f) else SubStroke.copy(alpha = 0.58f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.name, color = Color.White, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(plan.billingPeriod, color = Muted, fontSize = 12.sp)
                    if (plan.highlighted) {
                        Text(
                            "Best value",
                            color = Color.Black,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 5.dp).clip(RoundedCornerShape(50)).background(Lime).padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(plan.price, color = accent, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                    if (isSelected) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Lime), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
            plan.features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                    Text(feature, color = Color.White.copy(alpha = 0.76f), fontSize = 13.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AccentIcon(icon: ImageVector, accent: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
    }
}
