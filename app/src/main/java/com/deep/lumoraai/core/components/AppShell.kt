package com.deep.lumoraai.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import com.deep.lumoraai.core.notification.NotificationViewModel

/**
 * App Shell that provides consistent layout structure for all main screens
 * Includes top header (credits + notifications), bottom navigation, and footer
 * Excludes auth screens (Splash, Language, Onboarding, Auth)
 */
@Composable
fun AppShell(
    title: String,
    modifier: Modifier = Modifier,
    currentRoute: String,
    userCredits: Int = 500, // TODO: Connect to UserCreditsViewModel
    onNotifications: () -> Unit,
    onNavigate: (String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    showBottomNav: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    // Responsive design: show bottom nav only on mobile screens
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val shouldShowBottomNav = showBottomNav && screenWidth < 600 // Material3 mobile breakpoint

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UserHeaderBar(
                title = title,
                userCredits = userCredits,
                unreadNotificationCount = 0, // TODO: Get from NotificationViewModel
                onNotifications = onNotifications,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            if (shouldShowBottomNav) {
                BottomNavigationBar(
                    items = emptyList(),
                    selected = currentRoute,
                    onSelected = onNavigate
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding()
            ) {
                content(padding)
            }
            
            // Footer
            AppFooter()
        }
    }
}