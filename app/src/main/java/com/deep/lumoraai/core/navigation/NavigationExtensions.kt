package com.deep.lumoraai.core.navigation

import android.net.Uri
import androidx.navigation.NavHostController

fun NavHostController.goTo(route: String) {
    val destination = when (route) {
        Screen.CreateHub.route -> createHubRoute()
        else -> route
    }
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

fun Screen.nextScreen(): Screen {
    val index = navigationSequence.indexOfFirst { it.route == route }
    return navigationSequence[(index + 1).coerceAtMost(navigationSequence.lastIndex)]
}