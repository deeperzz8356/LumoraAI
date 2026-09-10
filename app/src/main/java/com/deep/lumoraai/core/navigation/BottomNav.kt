package com.deep.lumoraai.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.deep.lumoraai.core.components.BottomNavigationBar

@Composable
fun BottomNav(currentRoute: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    BottomNavigationBar(
        items = listOf(Screen.Home.route, Screen.Templates.route, Screen.History.route),
        selected = currentRoute,
        onSelected = onNavigate,
        modifier = modifier
    )
}
