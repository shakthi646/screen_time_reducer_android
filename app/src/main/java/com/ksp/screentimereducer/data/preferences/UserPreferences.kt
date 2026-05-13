package com.ksp.screentimereducer.data.preferences

/**
 * Snapshot of the user's persisted preferences. Held in memory by the
 * preferences repository and streamed from DataStore.
 */
data class UserPreferences(
    val onboardingComplete: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val defaultDelaySeconds: Int = 5,
    val focusStreak: Int = 0,
    val totalXp: Int = 0,
    val lastFocusCompletedDate: Long = 0L,
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }
