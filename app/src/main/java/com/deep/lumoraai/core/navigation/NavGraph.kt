package com.deep.lumoraai.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deep.lumoraai.data.repository.AuthRepository
import com.deep.lumoraai.core.utils.GuestIdentity
import com.deep.lumoraai.feature.auth.AuthRoute
import com.deep.lumoraai.feature.bgstudio.BgStudioRoute
import com.deep.lumoraai.feature.compress.CompressRoute
import com.deep.lumoraai.feature.createhub.CreateHubRoute
import com.deep.lumoraai.feature.credits.CreditsRoute
import com.deep.lumoraai.feature.history.HistoryRoute
import com.deep.lumoraai.feature.home.HomeRoute
import com.deep.lumoraai.feature.imagetoimage.ImageToImageRoute
import com.deep.lumoraai.feature.imagetovideo.ImageToVideoRoute
import com.deep.lumoraai.feature.language.LanguageRoute
import com.deep.lumoraai.feature.notifications.NotificationsRoute
import com.deep.lumoraai.feature.onboarding.OnboardingRoute
import com.deep.lumoraai.feature.photoenhance.PhotoEnhanceRoute
import com.deep.lumoraai.feature.profile.EDIT_PROFILE_ROUTE
import com.deep.lumoraai.feature.profile.EditProfileScreen
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
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    notificationRoute: String? = null,
    onNotificationRouteConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    fun next(screen: Screen) = { navController.goTo(screen.nextScreen().route) }

    LaunchedEffect(notificationRoute) {
        val route = notificationRoute?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        navController.goTo(route)
        onNotificationRouteConsumed()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = { lumoraEnterTransition() },
        exitTransition = { lumoraExitTransition() },
        popEnterTransition = { lumoraPopEnterTransition() },
        popExitTransition = { lumoraPopExitTransition() },
    ) {
        composable(Screen.Splash.route) {
            SplashRoute(
                onNext = {
                    val user = FirebaseAuth.getInstance().currentUser
                    val target = if (user != null) Screen.Home.route else Screen.Language.route
                    navController.goTo(target)
                }
            )
        }
        composable(
            route = Screen.Language.route + "?source={source}",
            arguments = listOf(
                navArgument("source") {
                    type = NavType.StringType
                    defaultValue = "onboarding"
                }
            )
        ) { backStackEntry ->
            val source = backStackEntry.arguments?.getString("source")
            LanguageRoute(
                onNext = {
                    if (source == "settings") {
                        navController.popBackStack()
                    } else {
                        navController.goTo(Screen.Onboarding.route)
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingRoute(
                onNext = {
                    coroutineScope.launch {
                        if (FirebaseAuth.getInstance().currentUser == null) {
                            if (GuestIdentity.isTrialExhausted(context)) {
                                navController.goTo(Screen.Auth.route)
                                return@launch
                            } else {
                                GuestIdentity.markTrialStarted(context)
                                AuthRepository().loginAnonymouslyAndSync()
                            }
                        }
                        navController.goTo(Screen.Home.route)
                    }
                }
            )
        }
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
        composable(
            route = Screen.TextToImage.route + "?prompt={prompt}",
            arguments = listOf(
                navArgument("prompt") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val prompt = backStackEntry.arguments?.getString("prompt")
            TextToImageRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) },
                initialPrompt = prompt,
            )
        }
        composable(Screen.ImageToImage.route) {
            ImageToImageRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) }
            )
        }
        composable(
            route = Screen.BgStudio.route + "?mode={mode}",
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "replace"
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "replace"
            BgStudioRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) },
                initialMode = mode,
            )
        }
        composable(Screen.PhotoEnhance.route) {
            PhotoEnhanceRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) }
            )
        }
        composable(Screen.Compress.route) {
            CompressRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) }
            )
        }
        composable(Screen.ImageToVideo.route) {
            ImageToVideoRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) }
            )
        }
        composable(
            route = Screen.TextToVideo.route + "?prompt={prompt}",
            arguments = listOf(
                navArgument("prompt") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val prompt = backStackEntry.arguments?.getString("prompt")
            TextToVideoRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) },
                initialPrompt = prompt,
            )
        }
        composable(
            route = Screen.PromoVideo.route + "?prompt={prompt}",
            arguments = listOf(
                navArgument("prompt") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val prompt = backStackEntry.arguments?.getString("prompt")
            TextToVideoRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) },
                isPromo = true,
                initialPrompt = prompt,
            )
        }
        composable(Screen.Templates.route) { TemplatesRoute(onNext = next(Screen.Templates), onNavigate = { navController.goTo(it) }) }
        composable(Screen.Queue.route) { QueueRoute(onNext = next(Screen.Queue), onNavigate = { navController.goTo(it) }) }
        composable(Screen.Result.route) { ResultRoute(onNext = next(Screen.Result)) }
        composable(Screen.History.route) {
            HistoryRoute(onNext = next(Screen.History), onNavigate = { route -> navController.navigate(route) })
        }
        composable(Screen.Credits.route) {
            CreditsRoute(
                onNext = next(Screen.Credits),
                onNavigate = { navController.goTo(it) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationsRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) }
            )
        }
        composable(Screen.Subscription.route) {
            SubscriptionRoute(
                onNavigate = { navController.goTo(it) },
                onBack = { navController.popBackStack() }
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
        composable(EDIT_PROFILE_ROUTE) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }
    }
}

private const val NavTransitionMillis = 260

private fun AnimatedContentTransitionScope<*>.lumoraEnterTransition(): EnterTransition =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(NavTransitionMillis, easing = FastOutSlowInEasing)
    ) + fadeIn(tween(180)) + scaleIn(
        initialScale = 0.985f,
        animationSpec = tween(NavTransitionMillis, easing = FastOutSlowInEasing)
    )

private fun AnimatedContentTransitionScope<*>.lumoraExitTransition(): ExitTransition =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(NavTransitionMillis, easing = FastOutSlowInEasing),
        targetOffset = { it / 5 }
    ) + fadeOut(tween(150)) + scaleOut(
        targetScale = 0.99f,
        animationSpec = tween(NavTransitionMillis, easing = FastOutSlowInEasing)
    )

private fun AnimatedContentTransitionScope<*>.lumoraPopEnterTransition(): EnterTransition =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(NavTransitionMillis, easing = FastOutSlowInEasing),
        initialOffset = { it / 5 }
    ) + fadeIn(tween(180)) + scaleIn(
        initialScale = 0.99f,
        animationSpec = tween(NavTransitionMillis, easing = FastOutSlowInEasing)
    )

private fun AnimatedContentTransitionScope<*>.lumoraPopExitTransition(): ExitTransition =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(NavTransitionMillis, easing = FastOutSlowInEasing)
    ) + fadeOut(tween(150)) + scaleOut(
        targetScale = 0.985f,
        animationSpec = tween(NavTransitionMillis, easing = FastOutSlowInEasing)
    )
