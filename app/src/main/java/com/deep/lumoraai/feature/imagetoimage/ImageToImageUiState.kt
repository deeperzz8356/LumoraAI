package com.deep.lumoraai.feature.imagetoimage

import android.graphics.Bitmap
import com.deep.lumoraai.feature.generation.GenerationAspectRatio

data class ImageToImageUiState(
    val sourceBitmap: Bitmap? = null,
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedStyle: ImageStyle = ImageStyle.NoStyle,
    val aspectRatio: GenerationAspectRatio = GenerationAspectRatio.Portrait,
    val similarity: Float = 0.5f,
    val generations: Int = 2,
    val isGenerating: Boolean = false,
    val generationProgress: Float? = null,
    val generationStatusText: String? = null,
    val isImprovingPrompt: Boolean = false,
    val generatedPath: String? = null,
    val generatedPaths: List<String> = emptyList(),
    val generatedMimeType: String = "image/png",
    val error: String? = null,
)

enum class ImageStyle(val label: String, val promptHint: String, val assetFileName: String) {
    NoStyle("No Style", "follow only the prompt without applying a preset look", "nostyle.jpg"),
    Photorealistic("Photorealistic", "realistic camera look, natural skin/materials, true lighting", "photorealitstic.png"),
    Cinematic("Cinematic", "movie-poster lighting, dramatic contrast, depth, atmospheric effects", "cinematic.png"),
    Anime("Anime", "clean anime illustration, expressive design, stylized backgrounds", "anime.png"),
    ThreeDRender("3D Render", "Blender/Octane-like materials, realistic reflections, polished CGI", "3d.png"),
    DigitalPainting("Digital Painting", "hand-painted concept-art feel, detailed brushwork", "digital art.png"),
    ComicBook("Comic Book", "bold outlines, ink shading, dynamic panels, graphic colors", "comic.png"),
    Minimalist("Minimalist", "simple composition, limited elements, clean backgrounds", "minimalistic.png"),
    Cyberpunk("Cyberpunk", "neon cities, futuristic tech, rain, purple/blue/pink lighting", "cyberpunk.png"),
    FantasyArt("Fantasy Art", "magical environments, castles, creatures, epic landscapes", "fantasy art.png"),
    RetroVintage("Retro/Vintage", "80s/90s aesthetic, film grain, faded colors, nostalgic design", "retro.png"),
}

val ImageStyle.apiStyle: String
    get() = if (this == ImageStyle.NoStyle) "Default" else label

val ImageStyle.promptDirective: String
    get() = if (this == ImageStyle.NoStyle) {
        ""
    } else {
        " Style: $label ($promptHint)."
    }

enum class VideoStyle(val label: String, val promptHint: String, val assetFileName: String) {
    NoStyle("No Style", "follow only the prompt without applying a preset motion style", "documentaryvideo.png"),
    CinematicFilm("Cinematic Film", "movie-quality shots, dramatic lighting, shallow depth of field", "cinematicvideo.png"),
    AnimeAnimation("Anime Animation", "animated anime scenes with expressive movement and backgrounds", "animevideo.png"),
    PhotorealisticLiveAction("Photorealistic Live Action", "realistic humans, environments, physics and camera motion", "photorealitsticvideo.png"),
    ThreeDCgiAnimation("3D CGI Animation", "Pixar/Blender-like rendered environments and character movement", "3dvideo.png"),
    MusicVideo("Music Video", "fast cuts, stylized lighting, creative camera movement, visual effects", "musicvideo.png"),
    CommercialAdvertisement("Commercial/Advertisement", "clean product shots, smooth transitions, premium lighting", "advertvideo.png"),
    Documentary("Documentary", "natural camera work, realistic environments, handheld or observational feel", "documentaryvideo.png"),
    SlowMotionCinematic("Slow-Motion Cinematic", "flowing fabric, particles, explosions, water, dramatic movement", "slowmovideo.png"),
    DroneAerial("Drone/Aerial", "sweeping landscape shots, flyovers, city or nature cinematography", "dronevideo.png"),
    ExperimentalSurreal("Experimental/Surreal", "dream transitions, morphing objects, impossible environments and abstract motion", "expivideo.png"),
}

val VideoStyle.apiStyle: String
    get() = if (this == VideoStyle.NoStyle) "Default" else label

val VideoStyle.promptDirective: String
    get() = if (this == VideoStyle.NoStyle) {
        ""
    } else {
        " Style: $label ($promptHint)."
    }
