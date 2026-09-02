package com.deep.lumoraai.feature.credits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.EmptyState
import com.deep.lumoraai.core.components.ErrorState
import com.deep.lumoraai.core.components.Loading
import com.deep.lumoraai.core.navigation.Screen

// Theme colors matching Home/Profile pages
private val CredBackground = Color(0xFF081020)
private val CredCard = Color(0xFF10192D)
private val CredStroke = Color(0xFF172238)
private val Lime = Color(0xFFD6FF2F)
private val Purple = Color(0xFF9C63FF)
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
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "credits",
                onSelected = { onNext() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CredBackground)
                .padding(padding)
        ) {
            when (uiState) {
                CreditsUiState.Loading -> Loading()
                is CreditsUiState.Error -> ErrorState(title = "Credits", message = uiState.message)
                is CreditsUiState.Success -> CreditsContent(
                    credits = uiState.credits,
                    isDeveloperMode = uiState.isDeveloperMode,
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
    onBuy: (Int) -> Unit,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = CredCard,
            border = BorderStroke(1.dp, CredStroke.copy(alpha = 0.72f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Current Balance", color = Muted, fontSize = 14.sp)
                Text(
                    if (isDeveloperMode) "Unlimited" else "$credits",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    if (isDeveloperMode) "Developer Mode" else "LUM",
                    color = Lime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(onClick = { onNavigate(Screen.Subscription.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("View subscription plans", color = Lime, fontWeight = FontWeight.Bold)
        }
        
        Text("Top Up Credits", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        
        CreditPackageCard(title = "Starter Pack", credits = 50, price = "$4.99", onBuy = { onBuy(50) })
        CreditPackageCard(title = "Creator Pack", credits = 150, price = "$12.99", onBuy = { onBuy(150) }, highlighted = true)
        CreditPackageCard(title = "Pro Pack", credits = 500, price = "$39.99", onBuy = { onBuy(500) })
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun CreditPackageCard(title: String, credits: Int, price: String, onBuy: () -> Unit, highlighted: Boolean = false) {
    val accentColor = if (highlighted) Lime else Purple
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = CredCard,
        border = BorderStroke(1.dp, if (highlighted) Lime.copy(alpha = 0.72f) else CredStroke.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("$credits Credits", color = Muted, fontSize = 14.sp)
            }
            Button(
                onClick = onBuy,
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(price, color = if (highlighted) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
