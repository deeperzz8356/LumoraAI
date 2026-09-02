package com.deep.lumoraai.core.deeplink

import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import com.deep.lumoraai.core.navigation.Screen
import java.net.URLDecoder

/**
 * Handles deep link navigation for notifications and other sources
 */
object DeepLinkHandler {
    private const val TAG = "DeepLinkHandler"
    private const val SCHEME = "lumora"

    /**
     * Handle a deep link URI
     */
    fun handleDeepLink(uri: Uri?, navController: NavController): Boolean {
        return try {
            when {
                uri == null -> {
                    Log.w(TAG, "URI is null")
                    false
                }
                uri.scheme == SCHEME -> handleLumoraDeepLink(uri, navController)
                uri.scheme == "http" || uri.scheme == "https" -> handleWebDeepLink(uri, navController)
                else -> {
                    Log.w(TAG, "Unknown scheme: ${uri.scheme}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling deep link: ${e.message}", e)
            false
        }
    }

    /**
     * Handle lumora:// deep links
     * Examples:
     * - lumora://task/task-123
     * - lumora://feature/feature-abc
     * - lumora://notifications
     * - lumora://profile
     */
    private fun handleLumoraDeepLink(uri: Uri, navController: NavController): Boolean {
        val host = uri.host ?: return false
        val pathSegments = uri.pathSegments

        return when (host) {
            "task" -> {
                if (pathSegments.isNotEmpty()) {
                    val taskId = pathSegments[0]
                    navigateToTask(navController, taskId)
                } else {
                    navigateToQueue(navController)
                }
            }

            "feature" -> {
                if (pathSegments.isNotEmpty()) {
                    val featureId = pathSegments[0]
                    navigateToFeature(navController, featureId)
                } else {
                    navigateToHome(navController)
                }
            }

            "notifications" -> {
                navigateToNotifications(navController)
            }

            "profile" -> {
                navigateToProfile(navController)
            }

            "settings" -> {
                navigateToSettings(navController)
            }

            "help" -> {
                navigateToHelp(navController)
            }

            "result" -> {
                if (pathSegments.isNotEmpty()) {
                    val resultId = pathSegments[0]
                    navigateToResult(navController, resultId)
                } else {
                    navigateToHistory(navController)
                }
            }

            else -> {
                Log.w(TAG, "Unknown deep link host: $host")
                false
            }
        }
    }

    /**
     * Handle web deep links (http/https)
     */
    private fun handleWebDeepLink(uri: Uri, navController: NavController): Boolean {
        Log.d(TAG, "Web deep link: $uri")
        // TODO: Implement web deep link handling if needed
        return false
    }

    // ============ Navigation Methods ============

    private fun navigateToTask(navController: NavController, taskId: String): Boolean {
        return try {
            Log.d(TAG, "Navigating to task: $taskId")
            // Navigate to Queue screen and pass task ID
            navController.navigate(
                "${Screen.Queue.route}?taskId=${URLDecoder.decode(taskId, "UTF-8")}"
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to task: ${e.message}", e)
            false
        }
    }

    private fun navigateToFeature(navController: NavController, featureId: String): Boolean {
        return try {
            Log.d(TAG, "Navigating to feature: $featureId")
            // Navigate to CreateHub or relevant feature screen
            navController.navigate(
                "${Screen.CreateHub.route}?featureId=${URLDecoder.decode(featureId, "UTF-8")}"
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to feature: ${e.message}", e)
            false
        }
    }

    private fun navigateToResult(navController: NavController, resultId: String): Boolean {
        return try {
            Log.d(TAG, "Navigating to result: $resultId")
            // Navigate to Result screen
            navController.navigate(
                "${Screen.Result.route}?resultId=${URLDecoder.decode(resultId, "UTF-8")}"
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to result: ${e.message}", e)
            false
        }
    }

    private fun navigateToNotifications(navController: NavController): Boolean {
        return try {
            Log.d(TAG, "Navigating to notifications")
            navController.navigate(Screen.Notifications.route)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to notifications: ${e.message}", e)
            false
        }
    }

    private fun navigateToProfile(navController: NavController): Boolean {
        return try {
            Log.d(TAG, "Navigating to profile")
            navController.navigate(Screen.Profile.route)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to profile: ${e.message}", e)
            false
        }
    }

    private fun navigateToSettings(navController: NavController): Boolean {
        return try {
            Log.d(TAG, "Navigating to settings")
            navController.navigate(Screen.Settings.route)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to settings: ${e.message}", e)
            false
        }
    }

    private fun navigateToHelp(navController: NavController): Boolean {
        return try {
            Log.d(TAG, "Navigating to help")
            navController.navigate(Screen.HelpSupport.route)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to help: ${e.message}", e)
            false
        }
    }

    private fun navigateToQueue(navController: NavController): Boolean {
        return try {
            Log.d(TAG, "Navigating to queue")
            navController.navigate(Screen.Queue.route)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to queue: ${e.message}", e)
            false
        }
    }

    private fun navigateToHistory(navController: NavController): Boolean {
        return try {
            Log.d(TAG, "Navigating to history")
            navController.navigate(Screen.History.route)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to history: ${e.message}", e)
            false
        }
    }

    private fun navigateToHome(navController: NavController): Boolean {
        return try {
            Log.d(TAG, "Navigating to home")
            navController.navigate(Screen.Home.route)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to home: ${e.message}", e)
            false
        }
    }

    /**
     * Create a deep link URI for a given destination
     */
    fun createDeepLinkUri(destination: DeepLinkDestination): Uri {
        return when (destination) {
            is DeepLinkDestination.Task -> Uri.parse("lumora://task/${destination.taskId}")
            is DeepLinkDestination.Feature -> Uri.parse("lumora://feature/${destination.featureId}")
            is DeepLinkDestination.Result -> Uri.parse("lumora://result/${destination.resultId}")
            DeepLinkDestination.Notifications -> Uri.parse("lumora://notifications")
            DeepLinkDestination.Profile -> Uri.parse("lumora://profile")
            DeepLinkDestination.Settings -> Uri.parse("lumora://settings")
            DeepLinkDestination.Help -> Uri.parse("lumora://help")
            DeepLinkDestination.Home -> Uri.parse("lumora://home")
        }
    }
}

/**
 * Sealed class representing different deep link destinations
 */
sealed class DeepLinkDestination {
    data class Task(val taskId: String) : DeepLinkDestination()
    data class Feature(val featureId: String) : DeepLinkDestination()
    data class Result(val resultId: String) : DeepLinkDestination()
    data object Notifications : DeepLinkDestination()
    data object Profile : DeepLinkDestination()
    data object Settings : DeepLinkDestination()
    data object Help : DeepLinkDestination()
    data object Home : DeepLinkDestination()

    /**
     * Get the deep link URI string for this destination
     */
    fun toUri(): String = when (this) {
        is Task -> "lumora://task/$taskId"
        is Feature -> "lumora://feature/$featureId"
        is Result -> "lumora://result/$resultId"
        Notifications -> "lumora://notifications"
        Profile -> "lumora://profile"
        Settings -> "lumora://settings"
        Help -> "lumora://help"
        Home -> "lumora://home"
    }
}
