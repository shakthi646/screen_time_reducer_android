package com.ksp.screentimereducer.data.repository

import com.ksp.screentimereducer.data.preferences.PreferencesDataSource
import com.ksp.screentimereducer.data.preferences.ThemeMode
import com.ksp.screentimereducer.data.preferences.UserPreferences
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val source: PreferencesDataSource,
) : PreferencesRepository {
    override val preferences: Flow<UserPreferences> = source.flow
    override suspend fun setOnboardingComplete(value: Boolean) = source.setOnboardingComplete(value)
    override suspend fun setThemeMode(mode: ThemeMode) = source.setThemeMode(mode)
    override suspend fun setNotifications(enabled: Boolean) = source.setNotifications(enabled)
    override suspend fun setDefaultDelay(seconds: Int) = source.setDefaultDelay(seconds)
    override suspend fun setDynamicColors(enabled: Boolean) = source.setDynamicColors(enabled)
    override suspend fun addXp(amount: Int) = source.addXp(amount)
    override suspend fun recordFocusCompleted(streak: Int) =
        source.recordFocusCompleted(System.currentTimeMillis(), streak)
}
