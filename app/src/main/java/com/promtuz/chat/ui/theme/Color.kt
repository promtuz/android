package com.promtuz.chat.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

fun adjustLight(col: Color, changeInLight: Float): Color {
    val hsl = floatArrayOf(0f, 0f, 0f)
    ColorUtils.RGBToHSL(
        (col.red * 255f).toInt(),
        (col.green * 255f).toInt(),
        (col.blue * 255f).toInt(),
        hsl
    )
    hsl[2] += changeInLight
    return Color(ColorUtils.HSLToColor(hsl))
}