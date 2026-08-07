package com.deep.lumoraai.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deep.lumoraai.feature.auth.AuthRoute
import com.deep.lumoraai.feature.createhub.CreateHubRoute
import com.deep.lumoraai.feature.credits.CreditsRoute
import com.deep.lumoraai.feature.history.HistoryRoute
import com.deep.lumoraai.feature.home.HomeRoute
import com.deep.lumoraai.feature.imagetovideo.ImageToVideoRoute
import com.deep.lumoraai.feature.language.LanguageRoute
import com.deep.lumoraai.feature.notifications.NotificationsRoute
import com.deep.lumoraai.feature.onboarding.OnboardingRoute
import com.deep.lumoraai.feature.profile.ProfileRoute
import com.deep.lumoraai.feature.queue.QueueRoute
import com.deep.lumoraai.feature.result.ResultRoute
import com.deep.lumoraai.feature.settings.SettingsRoute
import com.deep.lumoraai.feature.splash.SplashRoute
import com.deep.lumoraai.feature.subscription.SubscriptionRoute
import com.deep.lumoraai.feature.templates.TemplatesRoute
import com.deep.lumoraai.feature.texttoimage.TextToImageRoute
import com.deep.lumoraai.feature.texttovideo.TextToVideoRoute
import com.google.firebase.auth.FirebaseAuth

import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    fun next(screen: Screen) = { navController.goTo(screen.nextScreen().route) }

    NavHost(navController = navController, startDestination = Screen.Splash.route, modifier = modifier) {
        composable(Screen.Splash.route) {
            SplashRoute(
                onNext = {
                    val user = FirebaseAuth.getInstance().currentUser
                    val target = if (user != null) Screen.Home.route else Screen.Language.route
                    navController.goTo(target)
                }
            )
        }
        composable(Screen.Language.route) { LanguageRoute(onNext = next(Screen.Language)) }
        composable(Screen.Onboarding.route) { OnboardingRoute(onNext = next(Screen.Onboarding)) }
        composable(Screen.Auth.route) { AuthRoute(onNext = next(Screen.Auth)) }
        composable(Screen.Home.route) { HomeRoute(onNext = next(Screen.Home), onNavigate = { navController.goTo(it) }) }
        composable(
            route = Screen.CreateHub.route + "?prompt={prompt}&tab={tab}",
            arguments = listOf(
                navArgument("prompt") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val prompt = backStackEntry.arguments?.getString("prompt")
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            CreateHubRoute(
                onNext = next(Screen.CreateHub),
                onNavigate = { navController.goTo(it) },
                initialPrompt = prompt,
                initialTab = tab
            )
        }
        composable(Screen.TextToImage.route) { TextToImageRoute(onNext = next(Screen.TextToImage)) }
        composable(Screen.ImageToVideo.route) { ImageToVideoRoute(onNext = next(Screen.ImageToVideo)) }
        composable(Screen.TextToVideo.route) { TextToVideoRoute(onNext = next(Screen.TextToVideo)) }
        composable(Screen.Templates.route) { TemplatesRoute(onNext = next(Screen.Templates), onNavigate = { navController.goTo(it) }) }
        composable(Screen.Queue.route) { QueueRoute(onNext = next(Screen.Queue), onNavigate = { navController.goTo(it) }) }
        composable(Screen.Result.route) { ResultRoute(onNext = next(Screen.Result)) }
        composable(Screen.History.route) { HistoryRoute(onNext = next(Screen.History)) }
        composable(Screen.Credits.route) { CreditsRoute(onNext = next(Screen.Credits)) }
        composable(Screen.Notifications.route) { NotificationsRoute(onNext = next(Screen.Notifications)) }
        composable(Screen.Subscription.route) {
            SubscriptionRoute(
                onNext = next(Screen.Subscription),
                onNavigate = { navController.goTo(it) },
            )
        }
        composable(Screen.Profile.route) {
            ProfileRoute(
                onNext = next(Screen.Profile),
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigate = { navController.goTo(it) }
            )
        }
        composable(Screen.Settings.route) { SettingsRoute(onNext = next(Screen.Settings), onNavigate = { navController.goTo(it) }) }
    }
}