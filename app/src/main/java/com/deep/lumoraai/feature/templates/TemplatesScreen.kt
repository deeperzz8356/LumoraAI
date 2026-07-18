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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.BottomNavigationBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import com.deep.lumoraai.core.navigation.Screen

data class TemplateItem(
    val id: String,
    val title: String,
    val prompt: String,
    val category: String,
    val imageRes: Int
)

@Composable
fun TemplatesScreen(
    uiState: TemplatesUiState,
    onNext: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFF0F1026), Color(0xFF070714))))
        ) {
            TemplatesContent(onNavigate = onNavigate)
        }
    }
}

@Composable
private fun TemplatesContent(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val templateItems = remember {
        listOf(
            TemplateItem(
                id = "tpl-1",
                title = "Cyberpunk Cathedral",
                prompt = "A breathtaking cyberpunk cathedral, neon glowing gothic architecture, flying cars hovering above, rain-slicked dark streets, hyper-detailed cyberpunk aesthetic.",
                category = "Fantasy",
                imageRes = R.drawable.style_digital
            ),
            TemplateItem(
                id = "tpl-2",
                title = "Fantasy Elven Valley",
                prompt = "A mystical elven valley with waterfalls, glowing bioluminescent flora, ancient giant trees, ethereal sunlight filtering through leaves, fantasy scenery.",
                category = "Fantasy",
                imageRes = R.drawable.style_fantasy
            ),
            TemplateItem(
                id = "tpl-3",
                title = "Classic Anime Street",
                prompt = "A peaceful Tokyo street in anime style, cherry blossoms falling, late afternoon warm sunlight casting long shadows, highly detailed retro anime vibe.",
                category = "Anime",
                imageRes = R.drawable.style_anime
            ),
            TemplateItem(
                id = "tpl-4",
                title = "Vibrant Cartoon Landscape",
                prompt = "A colorful, vibrant cartoon meadow with rolling green hills, fluffy white clouds in a bright blue sky, whimsical trees, high-quality cartoon background.",
                category = "Wallpapers",
                imageRes = R.drawable.style_cartoon
            )
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All Styles") }

    val filteredTemplates = remember(searchQuery, selectedFilter) {
        templateItems.filter { item ->
            val matchesFilter = selectedFilter == "All Styles" || item.category.equals(selectedFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || 
                item.title.contains(searchQuery, ignoreCase = true) || 
                item.prompt.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TemplatesTopBar()
        TemplatesHeader()
        TemplatesSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
        FilterPillsHorizontal(selectedFilter = selectedFilter, onFilterSelect = { selectedFilter = it })
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            filteredTemplates.forEach { item ->
                TemplateCard(
                    item = item,
                    onCopyClick = { promptText ->
                        val clip = ClipData.newPlainText("prompt", promptText)
                        clipboardManager.setPrimaryClip(clip)
                    },
                    onCreateClick = { promptText ->
                        onNavigate(Screen.CreateHub.route + "?prompt=" + promptText)
                    }
                )
            }
            if (filteredTemplates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No templates found matching your search.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun TemplatesTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Lumina AI", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun TemplatesHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Image Templates", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Accelerate your workflow with professional, AI-optimized prompts and layouts across cinematic styles.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun TemplatesSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search templates...", color = Color.White.copy(alpha = 0.3f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f)) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF161838),
            unfocusedContainerColor = Color(0xFF161838),
            focusedBorderColor = Color(0xFFA855F7).copy(alpha = 0.5f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}

@Composable
private fun FilterPillsHorizontal(selectedFilter: String, onFilterSelect: (String) -> Unit) {
    val filters = listOf("All Styles", "Wallpapers", "Anime", "Fantasy")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) Color(0xFFCFBDFF) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        BorderStroke(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f)),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onFilterSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    color = if (isSelected) Color(0xFF0F1026) else Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    item: TemplateItem,
    onCopyClick: (String) -> Unit,
    onCreateClick: (String) -> Unit
) {
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161838), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(item.category, color = Color(0xFFCFBDFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(item.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Text(
            text = item.prompt,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onCopyClick(item.prompt)
                    copied = true
                },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (copied) Color(0xFFADF021).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, if (copied) Color(0xFFADF021).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = if (copied) "Copied!" else "Copy Prompt",
                    color = if (copied) Color(0xFFADF021) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { onCreateClick(item.prompt) },
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFBDFF))
            ) {
                Text(
                    text = "Create with this",
                    color = Color(0xFF0F1026),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}