package com.deep.lumoraai.feature.imagetoimage

import android.graphics.Bitmap
import android.net.Uri
import com.deep.lumoraai.feature.generation.GenerationAspectRatio

data class ImageToImageSource(
    val id: String,
    val uri: Uri,
    val bitmap: Bitmap,
    val base64: String,
)

data class ImageToImageUiState(
    val sourceImages: List<ImageToImageSource> = emptyList(),
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedStyle: ImageStyle = ImageStyle.NoStyle,
    val aspectRatio: GenerationAspectRatio = GenerationAspectRatio.Portrait,
    val similarity: Float = 0.5f,
    val generations: Int = 2,
    val isLoadingSources: Boolean = false,
    val isGenerating: Boolean = false,
    val generationProgress: Float? = null,
    val generationStatusText: String? = null,
    val isImprovingPrompt: Boolean = false,
    val generatedPath: String? = null,
    val generatedPaths: List<String> = emptyList(),
    val generatedMimeType: String = "image/png",
    val error: String? = null,
)

enum class ImageStyle(
    val label: String,
    val promptHint: String,
    val assetFileName: String,
    @androidx.annotation.StringRes val labelRes: Int,
    @androidx.annotation.StringRes val descriptionRes: Int,
) {
    NoStyle("No Style", "follow only the prompt without applying a preset look", "nostyle.jpg", com.deep.lumoraai.R.string.ui_style_no_style, com.deep.lumoraai.R.string.ui_style_desc_no_style_image),
    Photorealistic("Photorealistic", "realistic camera look, natural skin/materials, true lighting", "photorealitstic.png", com.deep.lumoraai.R.string.ui_style_photorealistic, com.deep.lumoraai.R.string.ui_style_desc_photorealistic),
    Cinematic("Cinematic", "movie-poster lighting, dramatic contrast, depth, atmospheric effects", "cinematic.png", com.deep.lumoraai.R.string.ui_style_cinematic, com.deep.lumoraai.R.string.ui_style_desc_cinematic),
    Anime("Anime", "clean anime illustration, expressive design, stylized backgrounds", "anime.png", com.deep.lumoraai.R.string.ui_style_anime, com.deep.lumoraai.R.string.ui_style_desc_anime),
    ThreeDRender("3D Render", "Blender/Octane-like materials, realistic reflections, polished CGI", "3d.png", com.deep.lumoraai.R.string.ui_style_3d_render, com.deep.lumoraai.R.string.ui_style_desc_3d_render),
    DigitalPainting("Digital Painting", "hand-painted concept-art feel, detailed brushwork", "digital art.png", com.deep.lumoraai.R.string.ui_style_digital_painting, com.deep.lumoraai.R.string.ui_style_desc_digital_painting),
    ComicBook("Comic Book", "bold outlines, ink shading, dynamic panels, graphic colors", "comic.png", com.deep.lumoraai.R.string.ui_style_comic_book, com.deep.lumoraai.R.string.ui_style_desc_comic_book),
    Minimalist("Minimalist", "simple composition, limited elements, clean backgrounds", "minimalistic.png", com.deep.lumoraai.R.string.ui_style_minimalist, com.deep.lumoraai.R.string.ui_style_desc_minimalist),
    Cyberpunk("Cyberpunk", "neon cities, futuristic tech, rain, purple/blue/pink lighting", "cyberpunk.png", com.deep.lumoraai.R.string.ui_style_cyberpunk, com.deep.lumoraai.R.string.ui_style_desc_cyberpunk),
    FantasyArt("Fantasy Art", "magical environments, castles, creatures, epic landscapes", "fantasy art.png", com.deep.lumoraai.R.string.ui_style_fantasy_art, com.deep.lumoraai.R.string.ui_style_desc_fantasy_art),
    RetroVintage("Retro/Vintage", "80s/90s aesthetic, film grain, faded colors, nostalgic design", "retro.png", com.deep.lumoraai.R.string.ui_style_retro_vintage, com.deep.lumoraai.R.string.ui_style_desc_retro_vintage),
}

val ImageStyle.apiStyle: String
    get() = if (this == ImageStyle.NoStyle) "Default" else label

val ImageStyle.promptDirective: String
    get() = if (this == ImageStyle.NoStyle) {
        ""
    } else {
        " Style: $label ($promptHint)."
    }

enum class VideoStyle(
    val label: String,
    val promptHint: String,
    val assetFileName: String,
    @androidx.annotation.StringRes val labelRes: Int,
    @androidx.annotation.StringRes val descriptionRes: Int,
) {
    // Uses the same local fallback asset as the image "No Style" option
    // (nostyle.jpg) instead of a random video thumbnail.
    NoStyle("No Style", "follow only the prompt without applying a preset motion style", "nostyle.jpg", com.deep.lumoraai.R.string.ui_style_no_style, com.deep.lumoraai.R.string.ui_style_desc_no_style_video),
    CinematicFilm("Cinematic Film", "movie-quality shots, dramatic lighting, shallow depth of field", "cinematicvideo.png", com.deep.lumoraai.R.string.ui_style_cinematic_film, com.deep.lumoraai.R.string.ui_style_desc_cinematic_film),
    AnimeAnimation("Anime Animation", "animated anime scenes with expressive movement and backgrounds", "animevideo.png", com.deep.lumoraai.R.string.ui_style_anime_animation, com.deep.lumoraai.R.string.ui_style_desc_anime_animation),
    PhotorealisticLiveAction("Photorealistic Live Action", "realistic humans, environments, physics and camera motion", "photorealitsticvideo.png", com.deep.lumoraai.R.string.ui_style_photorealistic_live_action, com.deep.lumoraai.R.string.ui_style_desc_photorealistic_live_action),
    ThreeDCgiAnimation("3D CGI Animation", "Pixar/Blender-like rendered environments and character movement", "3dvideo.png", com.deep.lumoraai.R.string.ui_style_3d_cgi_animation, com.deep.lumoraai.R.string.ui_style_desc_3d_cgi_animation),
    MusicVideo("Music Video", "fast cuts, stylized lighting, creative camera movement, visual effects", "musicvideo.png", com.deep.lumoraai.R.string.ui_style_music_video, com.deep.lumoraai.R.string.ui_style_desc_music_video),
    CommercialAdvertisement("Commercial/Advertisement", "clean product shots, smooth transitions, premium lighting", "advertvideo.png", com.deep.lumoraai.R.string.ui_style_commercial_advertisement, com.deep.lumoraai.R.string.ui_style_desc_commercial_advertisement),
    Documentary("Documentary", "natural camera work, realistic environments, handheld or observational feel", "documentaryvideo.png", com.deep.lumoraai.R.string.ui_style_documentary, com.deep.lumoraai.R.string.ui_style_desc_documentary),
    SlowMotionCinematic("Slow-Motion Cinematic", "flowing fabric, particles, explosions, water, dramatic movement", "slowmovideo.png", com.deep.lumoraai.R.string.ui_style_slow_motion_cinematic, com.deep.lumoraai.R.string.ui_style_desc_slow_motion_cinematic),
    DroneAerial("Drone/Aerial", "sweeping landscape shots, flyovers, city or nature cinematography", "dronevideo.png", com.deep.lumoraai.R.string.ui_style_drone_aerial, com.deep.lumoraai.R.string.ui_style_desc_drone_aerial),
    ExperimentalSurreal("Experimental/Surreal", "dream transitions, morphing objects, impossible environments and abstract motion", "expivideo.png", com.deep.lumoraai.R.string.ui_style_experimental_surreal, com.deep.lumoraai.R.string.ui_style_desc_experimental_surreal),
}

val VideoStyle.apiStyle: String
    get() = if (this == VideoStyle.NoStyle) "Default" else label

val VideoStyle.promptDirective: String
    get() = if (this == VideoStyle.NoStyle) {
        ""
    } else {
        " Style: $label ($promptHint)."
    }
