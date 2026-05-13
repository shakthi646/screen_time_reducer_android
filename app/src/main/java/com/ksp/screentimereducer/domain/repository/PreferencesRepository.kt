package com.ksp.screentimereducer.domain.repository

import com.ksp.screentimereducer.data.preferences.ThemeMode
import com.ksp.screentimereducer.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setOnboardingComplete(value: Boolean)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setNotifications(enabled: Boolean)
    suspend fun setDefaultDelay(seconds: Int)
    suspend fun setDynamicColors(enabled: Boolean)
    suspend fun addXp(amount: Int)
    suspend fun recordFocusCompleted(streak: Int)
}
