package com.promtuz.chat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.promtuz.chat.navigation.ScrollState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect


private const val BLUR_RADIUS = 32;

@Composable
fun BoxScope.BlurredBars(
    hazeState: HazeState,
    scrollState: MutableState<ScrollState>,
    height: Dp,
    alignment: Alignment
) {
    LaunchedEffect(scrollState) {
        println("Real Shit ${scrollState.value.scroll.coerceAtMost(BLUR_RADIUS)}")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .align(alignment)
            .hazeEffect(
                hazeState,

                style = HazeStyle(
                    blurRadius = 32.dp,
                    tint = HazeTint(
                        MaterialTheme.colorScheme.background,
                        BlendMode.Screen
                    ),
                    noiseFactor = 0.1f,
                )
            ),
    )
}