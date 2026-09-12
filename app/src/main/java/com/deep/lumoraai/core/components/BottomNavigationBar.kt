package com.deep.lumoraai.core.components

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import com.deep.lumoraai.databinding.CommonBottomNavigationBinding
import compose.icons.TablerIcons
import compose.icons.tablericons.History
import compose.icons.tablericons.Home
import compose.icons.tablericons.Search

@Composable
fun BottomNavigationBar(
    items: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    if (screenWidth >= 600) return

    AndroidView(
        factory = { CommonBottomNavigationBinding.inflate(LayoutInflater.from(it)).root },
        update = { root ->
            bindBottomNavigation(
                binding = CommonBottomNavigationBinding.bind(root),
                selected = selected,
                onSelected = onSelected,
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    )
}

private fun bindBottomNavigation(
    binding: CommonBottomNavigationBinding,
    selected: String,
    onSelected: (String) -> Unit,
) {
    bindNavItem(
        container = binding.homeItem,
        icon = binding.homeIcon,
        label = binding.homeLabel,
        route = "home",
        selected = selected,
        onSelected = onSelected,
        disableWhenSelected = true,
    )
    bindNavItem(
        container = binding.templatesItem,
        icon = binding.templatesIcon,
        label = binding.templatesLabel,
        route = "templates",
        selected = selected,
        onSelected = onSelected,
    )
    bindNavItem(
        container = binding.historyItem,
        icon = binding.historyIcon,
        label = binding.historyLabel,
        route = "history",
        selected = selected,
        onSelected = onSelected,
    )
    bindNavItem(
        container = binding.profileItem,
        icon = binding.profileIcon,
        label = binding.profileLabel,
        route = "profile",
        selected = selected,
        onSelected = onSelected,
    )
}

private fun bindNavItem(
    container: LinearLayout,
    icon: ComposeView,
    label: TextView,
    route: String,
    selected: String,
    onSelected: (String) -> Unit,
    disableWhenSelected: Boolean = false,
) {
    val isSelected = selected == route
    val color = if (isSelected) Color.rgb(214, 255, 47) else Color.rgb(138, 148, 169)
    val imageVector = when (route) {
        "home" -> TablerIcons.Home
        "templates" -> TablerIcons.Search
        "history" -> TablerIcons.History
        else -> Icons.Default.Person
    }
    bindTablerIcon(icon, imageVector, ComposeColor(color))
    label.setTextColor(color)
    label.alpha = if (isSelected) 1f else 0.74f
    container.contentDescription = label.text
    container.isSelected = isSelected
    container.isEnabled = !(disableWhenSelected && isSelected)
    container.setOnClickListener(
        if (container.isEnabled) View.OnClickListener { onSelected(route) } else null
    )
}

private fun bindTablerIcon(
    host: ComposeView,
    imageVector: ImageVector,
    tint: ComposeColor,
) {
    host.setContent {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.fillMaxSize()
        )
    }
}
