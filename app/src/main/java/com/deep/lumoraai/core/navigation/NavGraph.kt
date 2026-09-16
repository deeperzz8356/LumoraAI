package com.deep.lumoraai.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deep.lumoraai.core.components.AppShell
import com.deep.lumoraai.data.repository.AuthRepository
import com.deep.lumoraai.core.utils.GuestIdentity
import com.deep.lumoraai.core.utils.OnboardingPreferences
import com.deep.lumoraai.feature.auth.AuthRoute
import com.deep.lumoraai.feature.aitools.AIToolsRoute
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
import com.deep.lumoraai.feature.profile.ProfileRoute
import com.deep.lumoraai.feature.queue.QueueRoute
import com.deep.lumoraai.feature.result.ResultRoute
import com.deep.lumoraai.feature.profile.EditProfileRoute
import com.deep.lumoraai.feature.settings.HelpSupportRoute
import com.deep.lumoraai.feature.settings.PrivacySecurityRoute
import com.deep.lumoraai.feature.settings.SettingsRoute
import com.deep.lumoraai.feature.splash.SplashRoute
import com.deep.lumoraai.feature.subscription.SubscriptionRoute
import com.deep.lumoraai.feature.templates.TemplatesRoute
import com.deep.lumoraai.feature.templates.TemplateSectionRoute
import com.deep.lumoraai.feature.texttoimage.TextToImageMode
import com.deep.lumoraai.feature.texttoimage.TextToImageRoute
import com.deep.lumoraai.feature.texttovideo.TextToVideoRoute
import com.deep.lumoraai.feature.uninstall.UninstallConfirmRoute
import com.deep.lumoraai.feature.uninstall.UninstallSurveyRoute
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

    // Hoist a SINGLE persistent banner to the navigation root so it survives tab
    // switches and is never recreated. Only show it on the 5 primary tabs.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(notificationRoute, currentRoute) {
        val route = notificationRoute?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        if (currentRoute == null || currentRoute == Screen.Splash.route) return@LaunchedEffect
        navController.goTo(route)
        onNotificationRouteConsumed()
    }

    val showBanner = currentRoute != null && (
        currentRoute == "home" ||
        currentRoute == "templates" ||
        currentRoute == "aitools" ||
        currentRoute == "history" ||
        currentRoute == "profile" ||
        currentRoute.startsWith("createhub")
    )

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF081020))) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.weight(1f),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Screen.Splash.route) {
            SplashRoute(
                onNext = {
                    val user = FirebaseAuth.getInstance().currentUser
                    val pendingRoute = notificationRoute?.takeIf { it.isNotBlank() }
                    val target = pendingRoute ?: if (user != null && OnboardingPreferences.isCompleted(context)) {
                        Screen.Home.route
                    } else {
                        Screen.Language.route
                    }
                    navController.goTo(target)
                    if (pendingRoute != null) onNotificationRouteConsumed()
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
                source = source,
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
                        OnboardingPreferences.markCompleted(context)
                        navController.goTo(Screen.Home.route)
                    }
                },
            )
        }
        composable(Screen.Auth.route) { AuthRoute(onNext = { navController.goTo(Screen.Home.route) }) }
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
                onBack = { navController.popBackStack() },
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
        composable(
            route = Screen.Logo.route + "?prompt={prompt}",
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
                mode = TextToImageMode.Logo,
            )
        }
        composable(
            route = Screen.Avatar.route + "?prompt={prompt}",
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
                mode = TextToImageMode.Avatar,
            )
        }
        composable(
            route = Screen.ImageToImage.route + "?prompt={prompt}",
            arguments = listOf(
                navArgument("prompt") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            ImageToImageRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) },
                initialPrompt = backStackEntry.arguments?.getString("prompt"),
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
        composable(
            route = Screen.ImageToVideo.route + "?prompt={prompt}",
            arguments = listOf(
                navArgument("prompt") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            ImageToVideoRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) },
                initialPrompt = backStackEntry.arguments?.getString("prompt"),
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
        composable(
            route = Screen.TemplateSection.route + "/{categoryId}/{sectionId}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("sectionId") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            TemplateSectionRoute(
                categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty(),
                sectionId = backStackEntry.arguments?.getString("sectionId").orEmpty(),
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) },
            )
        }
        composable(Screen.AITools.route) { AIToolsRoute(onNavigate = { navController.goTo(it) }) }
        composable(Screen.Queue.route) { QueueRoute(onNext = next(Screen.Queue), onNavigate = { navController.goTo(it) }) }
        composable(
            route = "${Screen.Result.route}?path={path}&type={type}&mime={mime}&after={after}",
            arguments = listOf(
                navArgument("path") { type = NavType.StringType; defaultValue = "" },
                navArgument("type") { type = NavType.StringType; defaultValue = "IMAGE" },
                navArgument("mime") { type = NavType.StringType; defaultValue = "image/png" },
                navArgument("after") { type = NavType.IntType; defaultValue = -1 },
            ),
        ) { entry ->
            ResultRoute(
                path = entry.arguments?.getString("path").orEmpty(),
                mediaType = entry.arguments?.getString("type").orEmpty(),
                mimeType = entry.arguments?.getString("mime").orEmpty(),
                firstNewJobIndex = entry.arguments?.getInt("after") ?: -1,
                onBack = { navController.popBackStack() },
            )
        }
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
                onDeleteAccount = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigate = { navController.goTo(it) },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.goTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsRoute(
                onNext = next(Screen.Settings),
                onNavigate = { navController.goTo(it) },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.goTo(Screen.Profile.route)
                    }
                }
            )
        }
        composable(Screen.EditProfile.route) { 
            EditProfileRoute(onBack = { navController.popBackStack() })
        }
        composable(Screen.PrivacySecurity.route) { 
            PrivacySecurityRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) }
            )
        }
        composable(Screen.HelpSupport.route) { 
            HelpSupportRoute(
                onBack = { navController.popBackStack() },
                onNavigate = { navController.goTo(it) }
            )
        }
        composable(Screen.UninstallConfirm.route) {
            UninstallConfirmRoute(
                onBackHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UninstallConfirm.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onStillUninstall = { navController.goTo(Screen.UninstallSurvey.route) },
            )
        }
        composable(Screen.UninstallSurvey.route) {
            UninstallSurveyRoute(
                onBackHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.UninstallConfirm.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
        if (showBanner) {
            // Banner sits flush directly under the nav row (the nav bar no longer
            // adds its own bottom inset on primary tabs). The system-nav inset is
            // applied BELOW the banner so there's no black gap between them.
            com.deep.lumoraai.ads.banner.PersistentBannerSlot(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11192B)),
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF11192B))
                    .windowInsetsBottomHeight(
                        androidx.compose.foundation.layout.WindowInsets.navigationBars
                    ),
            )
        }
    } // end Column
}
