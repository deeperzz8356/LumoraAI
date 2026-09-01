package com.deep.lumoraai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Example navigation bar with icon-based tabs
 * Each tab has an icon centered in a circle and label text below
 */
@Composable
fun NavBarWithIconTabs() {
    // Track the currently selected tab
    val (selectedTab, setSelectedTab) = remember { mutableStateOf(0) }
    
    val tabs = listOf(
        Pair("Home", Icons.Default.Home),
        Pair("Search", Icons.Default.Search),
        Pair("Create", Icons.Default.Add),
        Pair("Settings", Icons.Default.Settings)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, (label, icon) ->
                NavTabWithIcon(
                    icon = icon,
                    label = label,
                    onClick = { setSelectedTab(index) },
                    isSelected = index == selectedTab,
                    circleSize = 56f,
                    modifier = Modifier
                        .weight(1f),
                    iconColor = Color.White,
                    circleBackgroundColor = Color(0xFF6200EE)
                )
            }
        }
    }
}

/**
 * Example showing how to use NavTabWithIcon in a full screen layout
 */
@Composable
fun NavBarScreenExample() {
    val (selectedTab, setSelectedTab) = remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Your main content area
        // ... main content here ...
        
        // Navigation bar at the bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color(0xFFFAFAFA),
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTabWithIcon(
                    icon = Icons.Default.Home,
                    label = "Home",
                    onClick = { setSelectedTab(0) },
                    isSelected = selectedTab == 0,
                    circleSize = 60f
                )
                NavTabWithIcon(
                    icon = Icons.Default.Search,
                    label = "Explore",
                    onClick = { setSelectedTab(1) },
                    isSelected = selectedTab == 1,
                    circleSize = 60f
                )
                NavTabWithIcon(
                    icon = Icons.Default.Add,
                    label = "Create",
                    onClick = { setSelectedTab(2) },
                    isSelected = selectedTab == 2,
                    circleSize = 60f
                )
                NavTabWithIcon(
                    icon = Icons.Default.Settings,
                    label = "Profile",
                    onClick = { setSelectedTab(3) },
                    isSelected = selectedTab == 3,
                    circleSize = 60f
                )
            }
        }
    }
}
