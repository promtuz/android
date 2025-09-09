package com.promtuz.chat.ui.constants

import androidx.compose.animation.core.EaseInOutCirc
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween

object Tweens {
    fun <T> microInteraction(): TweenSpec<T> {
        return tween(150, easing = EaseInOutCirc)
    }
}