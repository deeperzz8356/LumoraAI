package com.deep.lumoraai.core.navigation

import androidx.navigation.NavHostController

fun NavHostController.goTo(route: String) {
    navigate(route) { launchSingleTop = true }
}

fun Screen.nextScreen(): Screen {
    val index = navigationSequence.indexOfFirst { it.route == route }
    return navigationSequence[(index + 1).coerceAtMost(navigationSequence.lastIndex)]
}