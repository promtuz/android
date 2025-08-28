package com.promtuz.chat.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun navigate(backStack: NavBackStack, key: NavKey) {
    if (backStack.size >= 2 && backStack[backStack.size - 2] == key) {
        backStack.removeLastOrNull()
    } else if (backStack.last() != key) backStack.add(key)
}