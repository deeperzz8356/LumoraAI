package com.deep.lumoraai.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deep.lumoraai.ui.theme.tokens.Spacing

enum class CardVariant { Filled, Elevated, Outlined }

@Composable
fun AppCard(
    modifier: Modifier = Modifier, 
    onClick: (() -> Unit)? = null, 
    variant: CardVariant = CardVariant.Filled, 
    content: @Composable ColumnScope.() -> Unit
) {
    val inner: @Composable ColumnScope.() -> Unit = { 
        Column(Modifier.padding(Spacing.md), content = content) 
    }
    val shape = MaterialTheme.shapes.medium
    
    if (onClick != null) {
        when (variant) {
            CardVariant.Elevated -> ElevatedCard(onClick = onClick, modifier = modifier, shape = shape, content = inner)
            CardVariant.Outlined -> OutlinedCard(onClick = onClick, modifier = modifier, shape = shape, content = inner)
            else -> Card(
                onClick = onClick, 
                modifier = modifier, 
                shape = shape, 
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                content = inner
            )
        }
    } else {
        when (variant) {
            CardVariant.Elevated -> ElevatedCard(modifier = modifier, shape = shape, content = inner)
            CardVariant.Outlined -> OutlinedCard(modifier = modifier, shape = shape, content = inner)
            else -> Card(
                modifier = modifier, 
                shape = shape, 
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                content = inner
            )
        }
    }
}
