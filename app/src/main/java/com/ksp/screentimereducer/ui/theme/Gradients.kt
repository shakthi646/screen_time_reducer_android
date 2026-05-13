package com.ksp.screentimereducer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppGradients {
    val Hero: Brush
        get() = Brush.verticalGradient(listOf(MidnightBlue, DeepIndigo, DeepIndigoBright))

    val Soft: Brush
        get() = Brush.verticalGradient(listOf(CalmLavender, Color(0xFFFFFFFF)))

    val Focus: Brush
        get() = Brush.linearGradient(listOf(DeepIndigo, MidnightBlue))

    val Mint: Brush
        get() = Brush.linearGradient(listOf(GentleMint, SoftPurple))

    val Sunset: Brush
        get() = Brush.linearGradient(listOf(WarmAmber, SoftCoral))
}
