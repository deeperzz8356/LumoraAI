package com.deep.lumoraai.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.ui.theme.tokens.Spacing

@Composable
fun AppLoadingScreen(modifier: Modifier = Modifier) =
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF081020)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFD6FF2F))
    }

@Composable
fun AppEmptyScreen(
    title: String, 
    body: String, 
    modifier: Modifier = Modifier,
    illustration: (@Composable () -> Unit)? = null, 
    action: Pair<String, () -> Unit>? = null
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        illustration?.invoke()
        Spacer(Modifier.height(Spacing.lg))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            body, 
            style = MaterialTheme.typography.bodyMedium, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (action != null) { 
            Spacer(Modifier.height(Spacing.lg))
            AppButton(text = action.first, onClick = action.second) 
        }
    }
}

@Composable
fun AppErrorScreen(
    message: String, 
    onRetry: (() -> Unit)? = null, 
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.xl), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Info, contentDescription = null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(Spacing.md))
        Text("Something went wrong", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            message, 
            style = MaterialTheme.typography.bodyMedium, 
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (onRetry != null) { 
            Spacer(Modifier.height(Spacing.lg))
            AppButton("Try again", onRetry, variant = AppButtonVariant.Tonal) 
        }
    }
}
