package com.promtuz.chat.compositions

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack


val LocalBackStack = compositionLocalOf { NavBackStack() }