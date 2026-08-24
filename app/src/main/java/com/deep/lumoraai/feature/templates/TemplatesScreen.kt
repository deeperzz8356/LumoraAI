package com.deep.lumoraai.feature.templates

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deep.lumoraai.R
import com.deep.lumoraai.core.components.BottomNavigationBar
import com.deep.lumoraai.core.components.LumoraIntroBackground
import com.deep.lumoraai.core.navigation.createHubRoute
import com.deep.lumoraai.core.theme.IntroPalette
import com.deep.lumoraai.core.theme.IntroTypography
import kotlinx.coroutines.delay

private val TemplateSectionShape = RoundedCornerShape(20.dp)
private val TemplateCardShape = RoundedCornerShape(16.dp)
private val TemplateImageShape = RoundedCornerShape(14.dp)

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
        containerColor = IntroPalette.BackgroundBase,
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
        ) {
            LumoraIntroBackground()
            TemplatesContent(onNavigate = onNavigate)
        }
    }
}

@Composable
private fun TemplatesContent(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

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
            val matchesFilter = selectedFilter == "All Styles" ||
                item.category.equals(selectedFilter, ignoreCase = true)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            TemplatesTopBar()
            TemplatesHeader()
            TemplatesSearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            FilterPillsHorizontal(
                selectedFilter = selectedFilter,
                onFilterSelect = { selectedFilter = it }
            )
            filteredTemplates.forEach { item ->
                TemplateCard(
                    item = item,
                    onCopyClick = { promptText ->
                        val clip = ClipData.newPlainText("prompt", promptText)
                        clipboardManager.setPrimaryClip(clip)
                    },
                    onCreateClick = { promptText ->
                        onNavigate(createHubRoute(prompt = promptText))
                    }
                )
            }
            if (filteredTemplates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(IntroPalette.SurfaceRaised, TemplateSectionShape)
                        .border(1.dp, IntroPalette.BorderSubtle, TemplateSectionShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No templates found matching your search.",
                        style = IntroTypography.body
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TemplatesTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = IntroPalette.AccentLime,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lumora AI", style = IntroTypography.greetingName)
        }
        Icon(
            Icons.Default.Menu,
            contentDescription = "Open menu",
            tint = IntroPalette.TextPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun TemplatesHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntroPalette.SurfaceRaised.copy(alpha = 0.78f), TemplateSectionShape)
            .border(1.dp, IntroPalette.BorderSubtle, TemplateSectionShape)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .background(IntroPalette.PrimaryButton.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                .border(1.dp, IntroPalette.PrimaryButton.copy(alpha = 0.34f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "Premium prompt library",
                style = IntroTypography.badge.copy(color = IntroPalette.SecondaryText)
            )
        }
        Text(
            text = "Image Templates",
            style = IntroTypography.greetingName.copy(fontSize = 28.sp, lineHeight = 34.sp)
        )
        Text(
            text = "Curated prompts for cinematic images, polished scenes, and fast creation.",
            style = IntroTypography.body
        )
    }
}

@Composable
private fun TemplatesSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Search templates...",
                style = IntroTypography.body.copy(color = IntroPalette.TextLegal)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = IntroPalette.AccentLime.copy(alpha = 0.72f)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(26.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = IntroPalette.SurfaceRaised,
            unfocusedContainerColor = IntroPalette.SurfaceRaised,
            focusedBorderColor = IntroPalette.AccentLime.copy(alpha = 0.62f),
            unfocusedBorderColor = IntroPalette.BorderSubtle,
            focusedTextColor = IntroPalette.TextPrimary,
            unfocusedTextColor = IntroPalette.TextPrimary,
            cursorColor = IntroPalette.AccentLime
        ),
        singleLine = true
    )
}

@Composable
private fun FilterPillsHorizontal(selectedFilter: String, onFilterSelect: (String) -> Unit) {
    val filters = listOf("All Styles", "Wallpapers", "Anime", "Fantasy")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) IntroPalette.AccentLime else IntroPalette.SurfaceRaised,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isSelected) {
                                IntroPalette.AccentLime.copy(alpha = 0.72f)
                            } else {
                                IntroPalette.BorderSubtle
                            }
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onFilterSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    style = IntroTypography.creditsChip.copy(
                        color = if (isSelected) Color.Black else IntroPalette.TextMuted
                    )
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
            .background(IntroPalette.SurfaceRaised.copy(alpha = 0.9f), TemplateCardShape)
            .border(1.dp, IntroPalette.BorderSubtle, TemplateCardShape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(TemplateImageShape)
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                    .border(1.dp, IntroPalette.AccentLime.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.category,
                    style = IntroTypography.badge.copy(color = IntroPalette.AccentLime)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = IntroPalette.PrimaryButton.copy(alpha = 0.92f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.title,
                style = IntroTypography.sectionTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = item.prompt,
            style = IntroTypography.toolDescription.copy(color = IntroPalette.TextMuted),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
                .border(1.dp, IntroPalette.PrimaryButton.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                .padding(12.dp)
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
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (copied) {
                        IntroPalette.AccentLime.copy(alpha = 0.2f)
                    } else {
                        IntroPalette.BackgroundBase
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (copied) IntroPalette.AccentLime.copy(alpha = 0.6f) else IntroPalette.BorderSubtle
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = if (copied) IntroPalette.AccentLime else IntroPalette.TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (copied) "Copied" else "Copy",
                    style = IntroTypography.buttonLabel.copy(
                        color = if (copied) IntroPalette.AccentLime else IntroPalette.TextPrimary
                    )
                )
            }

            Button(
                onClick = { onCreateClick(item.prompt) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IntroPalette.AccentLime)
            ) {
                Text(
                    text = "Create",
                    style = IntroTypography.buttonLabel.copy(color = Color.Black)
                )
            }
        }
    }
}
