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

@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    fun next(screen: Screen) = { navController.goTo(screen.nextScreen().route) }

    NavHost(navController = navController, startDestination = Screen.Splash.route, modifier = modifier) {
        composable(Screen.Splash.route) {
            SplashRoute(
                onNext = {
                    val user = FirebaseAuth.getInstance().currentUser
                    val target = if (user != null) Screen.Home.route else Screen.Onboarding.route
                    navController.goTo(target)
                }
            )
        }
        composable(Screen.Onboarding.route) { OnboardingRoute(onNext = next(Screen.Onboarding)) }
        composable(Screen.Auth.route) { AuthRoute(onNext = next(Screen.Auth)) }
        composable(Screen.Home.route) { HomeRoute(onNext = next(Screen.Home)) }
        composable(Screen.CreateHub.route) { CreateHubRoute(onNext = next(Screen.CreateHub)) }
        composable(Screen.TextToImage.route) { TextToImageRoute(onNext = next(Screen.TextToImage)) }
        composable(Screen.ImageToVideo.route) { ImageToVideoRoute(onNext = next(Screen.ImageToVideo)) }
        composable(Screen.TextToVideo.route) { TextToVideoRoute(onNext = next(Screen.TextToVideo)) }
        composable(Screen.Templates.route) { TemplatesRoute(onNext = next(Screen.Templates)) }
        composable(Screen.Queue.route) { QueueRoute(onNext = next(Screen.Queue)) }
        composable(Screen.Result.route) { ResultRoute(onNext = next(Screen.Result)) }
        composable(Screen.History.route) { HistoryRoute(onNext = next(Screen.History)) }
        composable(Screen.Credits.route) { CreditsRoute(onNext = next(Screen.Credits)) }
        composable(Screen.Notifications.route) { NotificationsRoute(onNext = next(Screen.Notifications)) }
        composable(Screen.Subscription.route) { SubscriptionRoute(onNext = next(Screen.Subscription)) }
        composable(Screen.Profile.route) {
            ProfileRoute(
                onNext = next(Screen.Profile),
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Settings.route) { SettingsRoute(onNext = next(Screen.Settings)) }
    }
}