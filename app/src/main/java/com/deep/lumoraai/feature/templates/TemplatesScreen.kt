package com.deep.lumoraai.feature.templates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.AppEmptyScreen
import com.deep.lumoraai.core.components.AppErrorScreen
import com.deep.lumoraai.core.components.AppLoadingScreen
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.navigation.Screen
import com.deep.lumoraai.core.navigation.promoVideoRoute
import com.deep.lumoraai.core.navigation.textToImageRoute
import com.deep.lumoraai.core.navigation.textToVideoRoute

private val TemplateBackground = Color(0xFF081020)
private val TemplatePanel = Color(0xFF111A2D)
private val TemplateCard = Color(0xFF0E172A)
private val TemplateStroke = Color(0xFF1B2A44)
private val TemplateSelected = Color(0xFF57647A)
private val Lime = Color(0xFFD6FF2F)
private val Muted = Color(0xFF9BA6BA)

@Composable
fun TemplatesScreen(
    uiState: TemplatesUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TemplateBackground,
        bottomBar = {
            BottomNavigationBar(
                items = emptyList(),
                selected = "templates",
                onSelected = onNavigate
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TemplateBackground)
                .padding(padding)
        ) {
            when (uiState) {
                is TemplatesUiState.Loading -> AppLoadingScreen()
                is TemplatesUiState.Error -> AppErrorScreen(message = uiState.message)
                is TemplatesUiState.Empty -> AppEmptyScreen(title = "No Templates", body = "Templates will appear here.")
                is TemplatesUiState.Success -> TemplatesContent(uiState = uiState, onNavigate = onNavigate)
            }
        }
    }
}

@Composable
private fun TemplatesContent(uiState: TemplatesUiState.Success, onNavigate: (String) -> Unit) {
    var selectedType by remember { mutableStateOf("Image") }
    val templates = if (selectedType == "Image") uiState.imageTemplates else uiState.videoTemplates
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TemplatesTopBar(onNavigate = onNavigate)
        TemplateTypeTabs(selectedType = selectedType, onSelected = { selectedType = it })
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            templates.forEach { item ->
                TemplateRow(
                    item = item,
                    onCopy = {
                        clipboard.setText(AnnotatedString(item.prompt))
                        Toast.makeText(context, "Template prompt copied", Toast.LENGTH_SHORT).show()
                    },
                    onClick = {
                        val route = when {
                            item.kind == TemplateKind.Image -> textToImageRoute(item.prompt)
                            item.title.contains("Promo", ignoreCase = true) ||
                                item.title.contains("Ad", ignoreCase = true) -> promoVideoRoute(item.prompt)
                            else -> textToVideoRoute(item.prompt)
                        }
                        onNavigate(route)
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplatesTopBar(onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF2D77FF), CircleShape)
                    .clickable { onNavigate(Screen.Profile.route) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_avatar),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "LUMORIA AI",
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CreditsPill(onClick = { onNavigate(Screen.Credits.route) })
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onNavigate(Screen.Notifications.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFFDFF7F4),
                    modifier = Modifier.size(19.dp)
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .align(Alignment.TopEnd)
                        .background(Lime, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun CreditsPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("◉", color = Lime, fontSize = 9.sp, lineHeight = 9.sp)
        Text("1,250", color = Lime, fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TemplateTypeTabs(selectedType: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TemplatePanel)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TemplateTab(
            label = "Image",
            icon = Icons.Default.Image,
            selected = selectedType == "Image",
            onClick = { onSelected("Image") },
            modifier = Modifier.weight(1f)
        )
        TemplateTab(
            label = "Video",
            icon = Icons.Default.VideoLibrary,
            selected = selectedType == "Video",
            onClick = { onSelected("Video") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TemplateTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(7.dp))
            .background(if (selected) TemplateSelected else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.82f), modifier = Modifier.size(10.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.86f),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TemplateRow(item: TemplateListItem, onCopy: () -> Unit, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp),
        shape = RoundedCornerShape(7.dp),
        color = TemplateCard,
        border = BorderStroke(1.dp, TemplateStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 58.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.Black)
            ) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.subtitle,
                    color = Muted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Lime.copy(alpha = 0.12f))
                    .border(1.dp, Lime.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onCopy),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy template prompt",
                    tint = Lime,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
