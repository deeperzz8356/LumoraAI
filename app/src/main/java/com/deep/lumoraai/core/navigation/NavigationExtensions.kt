package com.deep.lumoraai.core.navigation

import android.net.Uri
import androidx.navigation.NavHostController

fun NavHostController.goTo(route: String) {
    val destination = when (route) {
        Screen.CreateHub.route -> createHubRoute()
        Screen.BgStudio.route -> bgStudioRoute()
        else -> route
    }
    val currentBase = currentDestination?.route?.substringBefore("?")
    if (!destination.contains("?") && currentBase == destination) return
    navigate(destination) { launchSingleTop = true }
}

fun createHubRoute(prompt: String? = null, tab: Int = 0): String =
    buildString {
        append(Screen.CreateHub.route)
        append("?tab=$tab")
        if (!prompt.isNullOrBlank()) {
            append("&prompt=${Uri.encode(prompt)}")
        }
    }

fun bgStudioRoute(mode: String = "replace"): String =
    "${Screen.BgStudio.route}?mode=${Uri.encode(mode)}"

fun textToImageRoute(prompt: String? = null): String =
    buildString {
        append(Screen.TextToImage.route)
        if (!prompt.isNullOrBlank()) {
            append("?prompt=${Uri.encode(prompt)}")
        }
    }

fun textToVideoRoute(prompt: String? = null): String =
    buildString {
        append(Screen.TextToVideo.route)
        if (!prompt.isNullOrBlank()) {
            append("?prompt=${Uri.encode(prompt)}")
        }
    }

fun promoVideoRoute(prompt: String? = null): String =
    buildString {
        append(Screen.PromoVideo.route)
        if (!prompt.isNullOrBlank()) {
            append("?prompt=${Uri.encode(prompt)}")
        }
    }

fun Screen.nextScreen(): Screen {
    val index = navigationSequence.indexOfFirst { it.route == route }
    return navigationSequence[(index + 1).coerceAtMost(navigationSequence.lastIndex)]
}
