package com.deep.lumoraai.core.navigation

sealed class Screen(val route: String, val title: String) {
    data object Splash : Screen("splash", "Splash")
    data object Language : Screen("language", "Language")
    data object Onboarding : Screen("onboarding", "Onboarding")
    data object Auth : Screen("auth", "Auth")
    data object Home : Screen("home", "Home")
    data object CreateHub : Screen("createhub", "Create Hub")
    data object BgStudio : Screen("bgstudio", "Bg Studio")
    data object PhotoEnhance : Screen("photoenhance", "Photo Enhancer")
    data object TextToImage : Screen("texttoimage", "Text To Image")
    data object ImageToVideo : Screen("imagetovideo", "Image To Video")
    data object TextToVideo : Screen("texttovideo", "Text To Video")
    data object Templates : Screen("templates", "Templates")
    data object Queue : Screen("queue", "Queue")
    data object Result : Screen("result", "Result")
    data object History : Screen("history", "History")
    data object Credits : Screen("credits", "Credits")
    data object Notifications : Screen("notifications", "Notifications")
    data object Subscription : Screen("subscription", "Subscription")
    data object Profile : Screen("profile", "Profile")
    data object Settings : Screen("settings", "Settings")
}

val navigationSequence = listOf(
    Screen.Splash,
    Screen.Language,
    Screen.Onboarding,
    Screen.Auth,
    Screen.Home,
    Screen.CreateHub,
    Screen.BgStudio,
    Screen.PhotoEnhance,
    Screen.TextToImage,
    Screen.ImageToVideo,
    Screen.TextToVideo,
    Screen.Templates,
    Screen.Queue,
    Screen.Result,
    Screen.History,
    Screen.Credits,
    Screen.Notifications,
    Screen.Subscription,
    Screen.Profile,
    Screen.Settings
)
