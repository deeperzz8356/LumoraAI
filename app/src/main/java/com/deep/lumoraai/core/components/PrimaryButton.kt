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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog as ComposeDialog
import com.deep.lumoraai.core.theme.Dimension
import com.deep.lumoraai.core.theme.IntroPalette
import com.deep.lumoraai.core.theme.IntroTypography
import com.deep.lumoraai.core.theme.LumoraPrimary
import com.deep.lumoraai.core.theme.LumoraSecondary
import com.deep.lumoraai.core.theme.LumoraTertiary

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier.height(Dimension.ButtonHeight), shape = RoundedCornerShape(8.dp)) { Text(text) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.height(Dimension.ButtonHeight), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Text(text) }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = modifier.height(Dimension.ButtonHeight).clip(RoundedCornerShape(8.dp)).background(Brush.horizontalGradient(listOf(LumoraPrimary, LumoraSecondary, LumoraTertiary)))
    ) { Text(text, color = Color(0xFF0B0D12), fontWeight = FontWeight.Bold) }
}

@Composable
fun AppToolbar(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    rightAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimension.ToolbarHeight)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (action != null) {
                Box(modifier = Modifier.width(48.dp)) {
                    action()
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (rightAction != null) {
            Box(modifier = Modifier.width(48.dp), contentAlignment = Alignment.CenterEnd) {
                rightAction()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(items: List<String>, selected: String, onSelected: (String) -> Unit, modifier: Modifier = Modifier) {
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
            NavItem(Icons.Default.Home, "Home", selected == "home", { onSelected("home") }, Modifier.weight(1f))
            NavItem(Icons.Default.Search, "Templates", selected == "templates", { onSelected("templates") }, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(72.dp))
            NavItem(Icons.Default.Search, "AI Tools", selected == "aitools", { onSelected("aitools") }, Modifier.weight(1f))
            NavItem(Icons.Default.History, "History", selected == "history", { onSelected("history") }, Modifier.weight(1f))
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
                Icon(Icons.Default.AutoAwesome, contentDescription = "Create", tint = Color.Black, modifier = Modifier.size(22.dp))
                Text("Create", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(iconSize))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = IntroTypography.navLabel.copy(color = tint.copy(alpha = labelAlpha)))
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "Search") {
    OutlinedTextField(value = query, onValueChange = onQueryChange, modifier = modifier.fillMaxWidth(), placeholder = { Text(placeholder) }, singleLine = true)
}

@Composable
fun PromptTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "Describe what you want to create") {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = modifier.fillMaxWidth().height(132.dp), placeholder = { Text(placeholder) })
}

@Composable
fun Loading(modifier: Modifier = Modifier, text: String = "Loading") {
    Column(modifier = modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator()
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) = StateBlock(title = title, message = message, modifier = modifier)

@Composable
fun ErrorState(title: String, message: String, modifier: Modifier = Modifier) = StateBlock(title = title, message = message, modifier = modifier)

@Composable
private fun StateBlock(title: String, message: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Dialog(title: String, message: String, onDismiss: () -> Unit) {
    ComposeDialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(message, style = MaterialTheme.typography.bodyMedium)
                PrimaryButton(text = "Done", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(progress = { progress }, modifier = modifier.fillMaxWidth())
}

@Composable
fun ToolCard(title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
