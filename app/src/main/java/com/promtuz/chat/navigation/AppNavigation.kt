package com.promtuz.chat.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.promtuz.chat.compositions.LocalBackStack
import com.promtuz.chat.ui.screens.ChatScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppNavKey : NavKey

object AppRoutes {
    @Serializable
    data object App : AppNavKey

    @Serializable
    data class ChatScreen(val userId: String) : AppNavKey

    @Serializable
    data class ProfileScreen(val userId: String) : AppNavKey

    @Serializable
    data object SettingScreen : AppNavKey


    // === AUTHENTICATION SCREENS START ===

    @Serializable
    data object LoginScreen : AppNavKey

    // === AUTHENTICATION SCREENS END ===
}


@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(AppRoutes.App)

    // androidx.

    CompositionLocalProvider(LocalBackStack provides backStack) {
        NavDisplay(
            backStack,
            entryDecorators = listOf(
                rememberSavedStateNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
                rememberSceneSetupNavEntryDecorator()
            ),
            entryProvider = { key ->
                when (key) {
                    is AppRoutes.App -> {
                        NavEntry(key, content = { HomeNavigation() })
                    }

                    is AppRoutes.ProfileScreen -> {
                        NavEntry(key, content = { Text("Sup Homie") })
                    }

                    is AppRoutes.ChatScreen -> {
                        NavEntry(key, content = { ChatScreen(key.userId) })
                    }

                    is AppRoutes.LoginScreen -> {
                        NavEntry(key, content = { Text("real") })
                    }

                    else -> throw RuntimeException("Invalid Screen")
                }
            },
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 3 }, // Subtle parallax effect
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            },
            popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 }, // Matching parallax
                    animationSpec = tween(250, easing = LinearOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(250, easing = LinearOutSlowInEasing)
                )
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(250, easing = LinearOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(250, easing = LinearOutSlowInEasing)
                )
            }
        )
    }
}