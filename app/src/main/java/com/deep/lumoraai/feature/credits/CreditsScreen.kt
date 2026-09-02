package com.deep.lumoraai.feature.credits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.restrictions.GenerationGate

private val CredBackground = Color(0xFF081020)
private val CredCard = Color(0xFF10192D)
private val CredStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
private val Pink = Color(0xFFFF3D9D)
private val Cyan = Color(0xFF20E6F2)
private val Muted = Color(0xFF94A0B8)
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun CreditsScreen(
    uiState: CreditsUiState,
    viewModel: CreditsViewModel,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CredBackground,
        topBar = {
            AppToolbar(
                title = Screen.Credits.title,
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
        },
        bottomBar = { BottomNavigationBar(emptyList(), "credits", onNavigate) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CredBackground)
                .padding(padding)
        ) {
            when (uiState) {
                CreditsUiState.Loading -> AppLoadingScreen()
                is CreditsUiState.Error -> AppErrorScreen(message = uiState.message)
                is CreditsUiState.Success -> CreditsContent(
                    credits = uiState.credits,
                    isDeveloperMode = uiState.isDeveloperMode,
                    onBack = onBack,
                    onBuy = { viewModel.buyCredits(it) },
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
private fun CreditsContent(
    credits: Int,
    isDeveloperMode: Boolean,
    onBack: () -> Unit,
    onBuy: (Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    val balanceLabel = if (isDeveloperMode || credits >= GenerationGate.DEVELOPER_MODE_CREDITS_DISPLAY) "Unlimited" else "$credits"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PageTopBar(title = "Credits", subtitle = "Fuel every generation", onBack = onBack)
        BalanceHero(balanceLabel = balanceLabel, isDeveloperMode = isDeveloperMode)

        Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.fillMaxWidth()) {
            CreditStatCard("Images", "1 credit", Icons.Default.Star, Purple, Modifier.weight(1f))
            CreditStatCard("Videos", "5 credits", Icons.Default.Bolt, Pink, Modifier.weight(1f))
        }

        Surface(
            onClick = { onNavigate(Screen.Subscription.route) },
            modifier = Modifier.fillMaxWidth().height(74.dp),
            shape = CardShape,
            color = CredCard,
            border = BorderStroke(1.dp, CredStroke.copy(alpha = 0.72f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    AccentIcon(Icons.Default.CreditCard, Cyan)
                    Column {
                        Text("Subscription Plans", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Monthly refills and Pro tools", color = Muted, fontSize = 11.sp)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Lime, modifier = Modifier.size(20.dp))
            }
        }

        Text("Top Up", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        CreditPackageCard("Starter", "50 credits", "$4.99", Purple, onBuy = { onBuy(50) })
        CreditPackageCard("Creator", "150 credits", "$12.99", Lime, highlighted = true, badge = "Popular", onBuy = { onBuy(150) })
        CreditPackageCard("Studio", "500 credits", "$39.99", Cyan, badge = "Best for video", onBuy = { onBuy(500) })

        Spacer(modifier = Modifier.height(2.dp))
    }
}

@Composable
private fun PageTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, color = Color.White, fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun BalanceHero(balanceLabel: String, isDeveloperMode: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(176.dp),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, Lime.copy(alpha = 0.32f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current Balance", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(balanceLabel, color = Color.White, fontSize = 40.sp, lineHeight = 44.sp, fontWeight = FontWeight.ExtraBold)
                Text(if (isDeveloperMode) "Developer mode active" else "LUM credits available", color = Lime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Lime.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Lime, modifier = Modifier.size(31.dp))
            }
        }
    }
}

@Composable
private fun CreditStatCard(title: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(96.dp),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, CredStroke.copy(alpha = 0.58f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AccentIcon(icon, accent)
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(value, color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CreditPackageCard(
    title: String,
    credits: String,
    price: String,
    accent: Color,
    highlighted: Boolean = false,
    badge: String? = null,
    onBuy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, if (highlighted) Lime.copy(alpha = 0.72f) else CredStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                AccentIcon(Icons.Default.CheckCircle, accent)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(credits, color = Muted, fontSize = 12.sp, lineHeight = 15.sp)
                    if (badge != null) {
                        Text(
                            badge,
                            color = if (highlighted) Color.Black else accent,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (highlighted) Lime else accent.copy(alpha = 0.14f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.height(40.dp).widthIn(min = 82.dp)
            ) {
                Text(price, color = if (accent == Lime) Color.Black else Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun AccentIcon(icon: ImageVector, accent: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
    }
}
