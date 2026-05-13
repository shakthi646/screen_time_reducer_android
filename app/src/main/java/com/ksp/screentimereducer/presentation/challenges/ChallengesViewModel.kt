package com.ksp.screentimereducer.presentation.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.screentimereducer.domain.model.Challenge
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import com.ksp.screentimereducer.domain.usecase.AbandonChallengeUseCase
import com.ksp.screentimereducer.domain.usecase.CompleteChallengeUseCase
import com.ksp.screentimereducer.domain.usecase.ObserveChallengesUseCase
import com.ksp.screentimereducer.domain.usecase.StartChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val totalXp: Int = 0,
    val streak: Int = 0,
)

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    observe: ObserveChallengesUseCase,
    prefs: PreferencesRepository,
    private val start: StartChallengeUseCase,
    private val complete: CompleteChallengeUseCase,
    private val abandon: AbandonChallengeUseCase,
) : ViewModel() {

    val state: StateFlow<ChallengesUiState> = combine(
        observe(),
        prefs.preferences,
    ) { challenges, p ->
        ChallengesUiState(
            challenges = challenges,
            totalXp = p.totalXp,
            streak = p.focusStreak,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChallengesUiState())

    fun start(id: String) = viewModelScope.launch { start.invoke(id) }
    fun complete(id: String) = viewModelScope.launch { complete.invoke(id) }
    fun abandon(id: String) = viewModelScope.launch { abandon.invoke(id) }
}
