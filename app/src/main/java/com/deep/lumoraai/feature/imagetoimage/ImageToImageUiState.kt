package com.deep.lumoraai.feature.imagetoimage

import android.graphics.Bitmap

data class ImageToImageUiState(
    val sourceBitmap: Bitmap? = null,
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedStyle: ImageStyle = ImageStyle.Photorealistic,
    val similarity: Float = 0.5f,
    val generations: Int = 2,
    val isGenerating: Boolean = false,
    val isImprovingPrompt: Boolean = false,
    val generatedPath: String? = null,
    val generatedMimeType: String = "image/png",
    val error: String? = null,
)

enum class ImageStyle(val label: String, val promptHint: String) {
    Photorealistic("Photorealistic", "realistic camera look, natural skin/materials, true lighting"),
    Cinematic("Cinematic", "movie-poster lighting, dramatic contrast, depth, atmospheric effects"),
    Anime("Anime", "clean anime illustration, expressive design, stylized backgrounds"),
    ThreeDRender("3D Render", "Blender/Octane-like materials, realistic reflections, polished CGI"),
    DigitalPainting("Digital Painting", "hand-painted concept-art feel, detailed brushwork"),
    ComicBook("Comic Book", "bold outlines, ink shading, dynamic panels, graphic colors"),
    Minimalist("Minimalist", "simple composition, limited elements, clean backgrounds"),
    Cyberpunk("Cyberpunk", "neon cities, futuristic tech, rain, purple/blue/pink lighting"),
    FantasyArt("Fantasy Art", "magical environments, castles, creatures, epic landscapes"),
    RetroVintage("Retro/Vintage", "80s/90s aesthetic, film grain, faded colors, nostalgic design"),
}

enum class VideoStyle(val label: String, val promptHint: String) {
    CinematicFilm("Cinematic Film", "movie-quality shots, dramatic lighting, shallow depth of field"),
    AnimeAnimation("Anime Animation", "animated anime scenes with expressive movement and backgrounds"),
    PhotorealisticLiveAction("Photorealistic Live Action", "realistic humans, environments, physics and camera motion"),
    ThreeDCgiAnimation("3D CGI Animation", "Pixar/Blender-like rendered environments and character movement"),
    MusicVideo("Music Video", "fast cuts, stylized lighting, creative camera movement, visual effects"),
    CommercialAdvertisement("Commercial/Advertisement", "clean product shots, smooth transitions, premium lighting"),
    Documentary("Documentary", "natural camera work, realistic environments, handheld or observational feel"),
    SlowMotionCinematic("Slow-Motion Cinematic", "flowing fabric, particles, explosions, water, dramatic movement"),
    DroneAerial("Drone/Aerial", "sweeping landscape shots, flyovers, city or nature cinematography"),
    ExperimentalSurreal("Experimental/Surreal", "dream transitions, morphing objects, impossible environments and abstract motion"),
}
