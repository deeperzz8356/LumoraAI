package com.deep.lumoraai.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.deep.lumoraai.R

enum class HomeToolDestination {
    CreateHubImage,
    CreateHubVideo,
    Templates,
    ComingSoon,
}

data class HomeToolItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String? = null,
    val destination: HomeToolDestination,
)

data class HomeTemplatePreview(
    val id: String,
    val title: String,
    val prompt: String,
    val imageRes: Int,
)

val homeImageTools = listOf(
    HomeToolItem("Text to Image", "Generate from prompt", Icons.Default.Star, "Popular", HomeToolDestination.CreateHubImage),
    HomeToolItem("Templates", "Quick start styles", Icons.Default.List, null, HomeToolDestination.Templates),
    HomeToolItem("Art Effects", "Style transfer", Icons.Default.Edit, null, HomeToolDestination.CreateHubImage),
    HomeToolItem("AI Background", "Replace scenery", Icons.Default.Share, "New", HomeToolDestination.CreateHubImage),
    HomeToolItem("Upscaler", "4K enhancement", Icons.Default.Search, "Pro", HomeToolDestination.ComingSoon),
    HomeToolItem("BG Remover", "Instant transparent", Icons.Default.Close, null, HomeToolDestination.ComingSoon),
)

val homeVideoTools = listOf(
    HomeToolItem("Image to Video", "Animate stills", Icons.Default.PlayArrow, null, HomeToolDestination.CreateHubVideo),
    HomeToolItem("Text to Video", "Full scene gen", Icons.Default.PlayArrow, "Pro", HomeToolDestination.CreateHubVideo),
    HomeToolItem("Video Templates", "Pre-made storyboards", Icons.Default.List, null, HomeToolDestination.CreateHubVideo),
    HomeToolItem("Video Ads", "Convert any link", Icons.Default.Share, "New", HomeToolDestination.CreateHubVideo),
)

data class HomeVideoFeature(
    val id: String,
    val label: String,
    val description: String,
    val tagline: String,
    val tags: List<String>,
    val rawResId: Int,
)

val homeVideoFeatures = listOf(
    HomeVideoFeature(
        "vid-frozen",
        "FROZEN TIME",
        "Girl in a Crowd",
        "Give yourself superpowers. Get studio-grade results.",
        listOf("Time Control", "VFX", "Cinematic"),
        R.raw.vid_1
    ),
    HomeVideoFeature(
        "vid-gravity",
        "GRAVITY CONTROL",
        "Girl Changes the World",
        "From prompt to cinematic vision in seconds.",
        listOf("Physics", "Action", "Premium"),
        R.raw.vid_2
    ),
    HomeVideoFeature(
        "vid-reality",
        "REALITY REWRITE",
        "Powerful Transformation",
        "Bring impossible shots to life.",
        listOf("Transformation", "Sci-Fi", "Advanced"),
        R.raw.vid_3
    ),
)

val homeTemplatePreviews = listOf(
    HomeTemplatePreview(
        id = "tpl-1",
        title = "Cyberpunk Cathedral",
        prompt = "A breathtaking cyberpunk cathedral, neon glowing gothic architecture, rain-slicked dark streets.",
        imageRes = R.drawable.style_digital
    ),
    HomeTemplatePreview(
        id = "tpl-2",
        title = "Fantasy Elven Valley",
        prompt = "A mystical elven valley with waterfalls, glowing bioluminescent flora, ethereal sunlight.",
        imageRes = R.drawable.style_fantasy
    ),
    HomeTemplatePreview(
        id = "tpl-3",
        title = "Classic Anime Street",
        prompt = "A peaceful Tokyo street in anime style, cherry blossoms falling, warm sunlight.",
        imageRes = R.drawable.style_anime
    ),
)
