package com.deep.lumoraai.feature.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val EDIT_PROFILE_ROUTE = "edit_profile"

fun NavGraphBuilder.editProfileRoute(navController: NavController) {
    composable(EDIT_PROFILE_ROUTE) {
        EditProfileScreen(
            onBack = { navController.popBackStack() }
        )
    }
}

fun NavController.navigateToEditProfile() {
    this.navigate(EDIT_PROFILE_ROUTE)
}
