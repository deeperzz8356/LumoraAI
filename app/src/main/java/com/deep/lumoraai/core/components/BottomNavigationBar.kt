package com.deep.lumoraai.core.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.core.theme.IntroPalette
import com.deep.lumoraai.core.theme.IntroTypography
import com.deep.lumoraai.core.theme.LumoraTheme
import androidx.compose.ui.res.stringResource

/**
 * Responsive bottom navigation bar
 * Visible only on mobile screens (<600dp width)
 */
@Composable
fun BottomNavigationBar(
    items: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Check screen size for responsive behavior
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Only show on mobile screens
    if (screenWidth < 600) {
        ResponsiveBottomNav(
            selected = selected,
            onSelected = onSelected,
            modifier = modifier
        )
    }
}

@Composable
private fun ResponsiveBottomNav(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val centerSize by animateDpAsState(
        targetValue = if (selected == "createhub") 62.dp else 58.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "centerCreateSize"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(88.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFF11192B))
                .border(BorderStroke(1.dp, Color(0xFF1B2A44))),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selected == "home",
                onClick = { onSelected("home") },
                modifier = Modifier.weight(1f)
            )
            NavItem(
                icon = Icons.Default.Search,
                label = "Templates",
                isSelected = selected == "templates",
                onClick = { onSelected("templates") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(72.dp))
            NavItem(
                icon = Icons.Default.AutoAwesome,
                label = "AI Tools",
                isSelected = selected == "aitools",
                onClick = { onSelected("aitools") },
                modifier = Modifier.weight(1f)
            )
            NavItem(
                icon = Icons.Default.History,
                label = "History",
                isSelected = selected == "history",
                onClick = { onSelected("history") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Box(
            modifier = Modifier
                .padding(bottom = 26.dp)
                .size(centerSize)
                .background(IntroPalette.AccentLime, CircleShape)
                .clickable { onSelected("createhub") },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = stringResource(com.deep.lumoraai.R.string.ui_create),
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "Create",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint by animateColorAsState(
        targetValue = if (isSelected) IntroPalette.AccentLime else Color(0xFF8A94A9),
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "navTint"
    )
    val iconSize by animateDpAsState(
        targetValue = if (isSelected) 26.dp else 24.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "navIconSize"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.74f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "navLabelAlpha"
    )
    val isEnabled = !(label == "Home" && isSelected)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clickable(enabled = isEnabled, onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = IntroTypography.navLabel.copy(color = tint.copy(alpha = labelAlpha))
        )
    }
}

@Preview(name = "Bottom Navigation - Mobile")
@Composable
private fun BottomNavigationBarPreview() {
    LumoraTheme {
        BottomNavigationBar(
            items = emptyList(),
            selected = "home",
            onSelected = {}
        )
    }
}
