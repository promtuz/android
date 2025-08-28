package com.promtuz.chat.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.promtuz.chat.compositions.LocalHazeState
import com.promtuz.chat.ui.components.BottomBar
import com.promtuz.chat.ui.components.TopBar
import com.promtuz.chat.ui.screens.CallScreen
import com.promtuz.chat.ui.screens.FriendScreen
import com.promtuz.chat.ui.screens.HomeScreen
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.serialization.Serializable


@Serializable
sealed interface HomeNavKey : NavKey

object HomeRoutes {
    @Serializable
    data object HomeScreen : HomeNavKey

    @Serializable
    data object CallScreen : HomeNavKey

    @Serializable
    data object FriendScreen : HomeNavKey
}

@Composable
fun HomeNavigation(defaultBackStack: List<HomeNavKey>) {
    val backStack = rememberNavBackStack(*defaultBackStack.toTypedArray())

    val localHazeState = rememberHazeState()
    val hazeState = rememberHazeState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(localHazeState),
        topBar = { TopBar() },
        bottomBar = { BottomBar(backStack) }
    ) { innerPadding ->
        CompositionLocalProvider(LocalHazeState provides localHazeState) {
            NavDisplay(
                backStack,
                entryDecorators = listOf(
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                    rememberSceneSetupNavEntryDecorator()
                ),
                entryProvider = { key ->
                    when (key) {
                        is HomeRoutes.HomeScreen -> {
                            NavEntry(key = key) {
                                HomeScreen(hazeState, innerPadding)
                            }
                        }

                        is HomeRoutes.CallScreen -> {
                            NavEntry(key = key) {
                                CallScreen(hazeState, innerPadding)
                            }
                        }

                        is HomeRoutes.FriendScreen -> {
                            NavEntry(key = key) {
                                FriendScreen(hazeState, innerPadding)
                            }
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
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            )
        }
    }
}