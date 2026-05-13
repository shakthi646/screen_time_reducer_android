package com.ksp.screentimereducer.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import com.ksp.screentimereducer.domain.usecase.SeedChallengesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val loading: Boolean = true,
    val onboardingComplete: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val seedChallenges: SeedChallengesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            seedChallenges()
            prefs.preferences.collect { p ->
                _state.update { it.copy(loading = false, onboardingComplete = p.onboardingComplete) }
            }
        }
    }

    fun complete() {
        viewModelScope.launch { prefs.setOnboardingComplete(true) }
    }
}
