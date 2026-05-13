package com.ksp.screentimereducer.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class PreferencesDataSource @Inject constructor(
    private val context: Context,
) {

    private object Keys {
        val ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_colors")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val DEFAULT_DELAY = intPreferencesKey("default_delay_seconds")
        val FOCUS_STREAK = intPreferencesKey("focus_streak")
        val XP = intPreferencesKey("total_xp")
        val LAST_FOCUS_DATE = longPreferencesKey("last_focus_date")
    }

    val flow: Flow<UserPreferences> = context.dataStore.data.map { p ->
        UserPreferences(
            onboardingComplete = p[Keys.ONBOARDING] ?: false,
            themeMode = runCatching { ThemeMode.valueOf(p[Keys.THEME] ?: ThemeMode.SYSTEM.name) }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColors = p[Keys.DYNAMIC] ?: false,
            notificationsEnabled = p[Keys.NOTIFICATIONS] ?: true,
            defaultDelaySeconds = p[Keys.DEFAULT_DELAY] ?: 5,
            focusStreak = p[Keys.FOCUS_STREAK] ?: 0,
            totalXp = p[Keys.XP] ?: 0,
            lastFocusCompletedDate = p[Keys.LAST_FOCUS_DATE] ?: 0L,
        )
    }

    suspend fun setOnboardingComplete(value: Boolean) =
        context.dataStore.edit { it[Keys.ONBOARDING] = value }.let { Unit }

    suspend fun setThemeMode(mode: ThemeMode) =
        context.dataStore.edit { it[Keys.THEME] = mode.name }.let { Unit }

    suspend fun setDynamicColors(value: Boolean) =
        context.dataStore.edit { it[Keys.DYNAMIC] = value }.let { Unit }

    suspend fun setNotifications(value: Boolean) =
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = value }.let { Unit }

    suspend fun setDefaultDelay(seconds: Int) =
        context.dataStore.edit { it[Keys.DEFAULT_DELAY] = seconds }.let { Unit }

    suspend fun addXp(amount: Int) {
        context.dataStore.edit { p ->
            p[Keys.XP] = (p[Keys.XP] ?: 0) + amount
        }
    }

    suspend fun recordFocusCompleted(timestamp: Long, newStreak: Int) {
        context.dataStore.edit { p ->
            p[Keys.FOCUS_STREAK] = newStreak
            p[Keys.LAST_FOCUS_DATE] = timestamp
        }
    }
}
