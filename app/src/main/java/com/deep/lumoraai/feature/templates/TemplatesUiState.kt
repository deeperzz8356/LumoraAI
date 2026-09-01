package com.deep.lumoraai.feature.templates

import com.deep.lumoraai.R

sealed interface TemplatesUiState {
    data object Loading : TemplatesUiState
    data class Success(
        val imageTemplates: List<TemplateListItem>,
        val videoTemplates: List<TemplateListItem>,
    ) : TemplatesUiState
    data class Error(val message: String) : TemplatesUiState
    data object Empty : TemplatesUiState
}

enum class TemplateKind {
    Image,
    Video,
}

data class TemplateListItem(
    val title: String,
    val subtitle: String,
    val prompt: String,
    val imageRes: Int,
    val kind: TemplateKind,
)

val realImageTemplates = listOf(
    TemplateListItem(
        title = "Cyberpunk Portrait",
        subtitle = "Neon cinematic character portrait",
        prompt = "A neon-drenched cyberpunk portrait of a confident futuristic creator, dramatic high-contrast lighting, wet city reflections, intricate face detail, premium cinematic color grading.",
        imageRes = R.drawable.style_digital,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Luxury Product Shot",
        subtitle = "Premium studio product image",
        prompt = "A luxury product hero shot on a glossy black surface, softbox reflections, rim lighting, shallow depth of field, clean premium commercial photography.",
        imageRes = R.drawable.onboarding_3_ill,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Fantasy Forest",
        subtitle = "Mystic glowing landscape",
        prompt = "An enchanted fantasy forest with glowing blue mist, ancient trees, tiny luminous flowers, cinematic moonlight, highly detailed environment concept art.",
        imageRes = R.drawable.style_fantasy,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Anime Hero",
        subtitle = "Stylized character poster",
        prompt = "A dynamic anime hero poster, wind-swept hair, intense expression, glowing energy effects, dramatic background, crisp linework and vibrant color.",
        imageRes = R.drawable.style_anime,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Glass UI Mockup",
        subtitle = "Futuristic app interface",
        prompt = "A polished glassmorphism mobile app dashboard, translucent cards, lime and cyan accents, deep dark background, premium AI product interface mockup.",
        imageRes = R.drawable.group_48096841,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Fashion Editorial",
        subtitle = "High-end magazine portrait",
        prompt = "A high-fashion editorial portrait with avant-garde styling, studio lighting, elegant pose, rich fabric texture, luxury magazine photography.",
        imageRes = R.drawable.onboarding_1_ill,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Food Commercial",
        subtitle = "Appetizing hero food photo",
        prompt = "A mouthwatering commercial food photograph, fresh ingredients, steam, glossy highlights, warm professional restaurant lighting, ultra realistic.",
        imageRes = R.drawable.onboarding_4_ill,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Sci-Fi Vehicle",
        subtitle = "Futuristic transport concept",
        prompt = "A sleek sci-fi vehicle speeding through a neon tunnel, motion blur, glowing headlights, aerodynamic black body, cinematic automotive concept render.",
        imageRes = R.drawable.onboarding_5_ill,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Logo Mascot",
        subtitle = "Clean brand character",
        prompt = "A friendly AI brand mascot logo, simple bold shapes, lime accent color, clean vector-like finish, modern startup identity style.",
        imageRes = R.drawable.logo,
        kind = TemplateKind.Image,
    ),
    TemplateListItem(
        title = "Minimal Poster",
        subtitle = "Premium event visual",
        prompt = "A minimal premium event poster design, bold central object, dark negative space, lime accent, elegant typography space, refined art direction.",
        imageRes = R.drawable.group_48096841,
        kind = TemplateKind.Image,
    ),
)

val realVideoTemplates = listOf(
    TemplateListItem(
        title = "Cinematic Product Spin",
        subtitle = "Smooth premium product reveal",
        prompt = "A cinematic product spin video with glossy reflections, dramatic rim light, slow camera orbit, premium studio background, elegant commercial pacing.",
        imageRes = R.drawable.onboarding_3_ill,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "AI Character Reveal",
        subtitle = "Dramatic character entrance",
        prompt = "A dramatic AI character reveal, glowing particles, slow push-in camera, neon atmosphere, confident stance, cinematic smoke and light movement.",
        imageRes = R.drawable.onboarding_1_ill,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "Travel Flythrough",
        subtitle = "Dream destination motion",
        prompt = "A dreamlike travel flythrough over a surreal glowing landscape, smooth drone camera, atmospheric fog, sunrise light, cinematic depth.",
        imageRes = R.drawable.onboarding_4_ill,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "Social Video Ad",
        subtitle = "Punchy promotional clip",
        prompt = "A punchy social media video ad with energetic camera motion, fast product reveal, bright highlights, clean premium brand visuals.",
        imageRes = R.drawable.onboarding_5_ill,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "Fashion Runway",
        subtitle = "Editorial motion scene",
        prompt = "A fashion runway video with confident model walk, soft spotlight sweeps, luxury editorial atmosphere, cinematic slow motion fabric movement.",
        imageRes = R.drawable.style_anime,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "Food Pour Shot",
        subtitle = "Restaurant commercial motion",
        prompt = "A restaurant commercial video showing sauce pouring over fresh food, steam rising, macro camera movement, appetizing warm light.",
        imageRes = R.drawable.style_fantasy,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "App Promo",
        subtitle = "Sleek SaaS launch clip",
        prompt = "A sleek mobile app promo video, floating interface panels, smooth parallax motion, dark premium UI, lime highlights, modern product launch feel.",
        imageRes = R.drawable.group_48096841,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "Car Tunnel Chase",
        subtitle = "Fast cinematic vehicle shot",
        prompt = "A futuristic sports car racing through a glowing tunnel, dynamic tracking camera, blue light trails, speed ramp motion, cinematic action.",
        imageRes = R.drawable.style_digital,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "Music Visualizer",
        subtitle = "Beat-reactive neon loop",
        prompt = "A neon music visualizer loop with pulsing light waves, abstract 3D shapes, rhythmic camera movement, dark club atmosphere.",
        imageRes = R.drawable.onboarding_2_ill,
        kind = TemplateKind.Video,
    ),
    TemplateListItem(
        title = "Promo Offer",
        subtitle = "Ad-ready sale visual",
        prompt = "A polished promotional offer video with premium product closeups, energetic transitions, bright accent lighting, social ad composition, strong final reveal.",
        imageRes = R.drawable.group_48096841,
        kind = TemplateKind.Video,
    ),
)
