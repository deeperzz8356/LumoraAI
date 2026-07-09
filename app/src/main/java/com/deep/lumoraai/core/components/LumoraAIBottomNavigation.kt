package com.deep.lumoraai.core.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LumoraAIBottomNavigation(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    BottomNavigationBar(items = listOf("home", "createhub", "queue", "profile"), selected = currentRoute, onSelected = onNavigate, modifier = modifier)
}