package com.ksp.screentimereducer.widget

data class WidgetSnapshot(
    val totalMs: Long = 0L,
    val unlocks: Int = 0,
    val streak: Int = 0,
    val focusActive: Boolean = false,
    val focusElapsedMs: Long = 0L,
)
