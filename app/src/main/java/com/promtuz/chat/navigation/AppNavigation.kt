package com.promtuz.chat.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppNavKey : NavKey

object AppRoutes {
    @Serializable
    data class App(val homeBackStack: List<HomeNavKey>) : AppNavKey

    @Serializable
    data object AuthScreen : AppNavKey

    @Serializable
    data class ProfileScreen(val userId: String) : AppNavKey

    @Serializable
    data object SettingScreen : AppNavKey
}

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(AppRoutes.App(listOf(HomeRoutes.HomeScreen)))

    NavDisplay(
        backStack, entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberSceneSetupNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (key) {
                is AppRoutes.App -> {
                    NavEntry(key = key) {
                        HomeNavigation(key.homeBackStack)
                    }
                }

                is AppRoutes.ProfileScreen -> {
                    NavEntry(key = key) {
                        Text("Sup Homie")
                    }
                }

                else -> throw RuntimeException("Invalid Screen")
            }
        }
    )
}