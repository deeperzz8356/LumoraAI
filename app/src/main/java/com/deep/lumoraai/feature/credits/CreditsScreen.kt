package com.deep.lumoraai.feature.credits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.components.AppToolbar
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.EmptyState
import com.deep.lumoraai.core.components.ErrorState
import com.deep.lumoraai.core.components.Loading
import com.deep.lumoraai.core.navigation.Screen

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
        topBar = {
            AppToolbar(
                title = "Credits",
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
                .background(Brush.verticalGradient(listOf(Color(0xFF0F1026), Color(0xFF070714))))
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161838), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Current Balance", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            Text(
                if (isDeveloperMode) "Unlimited" else "$credits",
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (isDeveloperMode) "Developer Mode" else "Credits",
                color = Color(0xFFA855F7),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(onClick = { onNavigate(Screen.Subscription.route) }, modifier = Modifier.fillMaxWidth()) {
            Text("View subscription plans", color = Color(0xFFCFBDFF))
        }
        
        Text("Top Up Credits", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        CreditPackageCard(title = "Starter Pack", credits = 50, price = "$4.99", onBuy = { onBuy(50) })
        CreditPackageCard(title = "Creator Pack", credits = 150, price = "$12.99", onBuy = { onBuy(150) }, highlighted = true)
        CreditPackageCard(title = "Pro Pack", credits = 500, price = "$39.99", onBuy = { onBuy(500) })
    }
}

@Composable
private fun CreditPackageCard(title: String, credits: Int, price: String, onBuy: () -> Unit, highlighted: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (highlighted) Color(0xFFA855F7).copy(alpha = 0.1f) else Color(0xFF161838), RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (highlighted) Color(0xFFA855F7) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("$credits Credits", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        }
        Button(
            onClick = onBuy,
            colors = ButtonDefaults.buttonColors(containerColor = if (highlighted) Color(0xFFA855F7) else Color(0xFFCFBDFF))
        ) {
            Text(price, color = if (highlighted) Color.White else Color(0xFF0F1026), fontWeight = FontWeight.Bold)
        }
    }
}