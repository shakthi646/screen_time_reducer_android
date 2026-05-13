package com.ksp.screentimereducer.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.screentimereducer.data.preferences.ThemeMode
import com.ksp.screentimereducer.data.preferences.UserPreferences
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
) : ViewModel() {

    val state: StateFlow<UserPreferences> =
        prefs.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setNotifications(enabled: Boolean) = viewModelScope.launch { prefs.setNotifications(enabled) }
    fun setDefaultDelay(seconds: Int) = viewModelScope.launch { prefs.setDefaultDelay(seconds) }
    fun setDynamic(enabled: Boolean) = viewModelScope.launch { prefs.setDynamicColors(enabled) }
}
