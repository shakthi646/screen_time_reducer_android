package com.ksp.screentimereducer.presentation.focus

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.screentimereducer.domain.model.AppRule
import com.ksp.screentimereducer.domain.model.FocusMode
import com.ksp.screentimereducer.domain.model.FocusSession
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import com.ksp.screentimereducer.domain.usecase.EndFocusSessionUseCase
import com.ksp.screentimereducer.domain.usecase.ObserveActiveFocusUseCase
import com.ksp.screentimereducer.domain.usecase.ObserveAppRulesUseCase
import com.ksp.screentimereducer.domain.usecase.ObserveFocusStatsUseCase
import com.ksp.screentimereducer.domain.usecase.StartFocusSessionUseCase
import com.ksp.screentimereducer.service.FocusSessionService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class FocusUiState(
    val active: FocusSession? = null,
    val rules: List<AppRule> = emptyList(),
    val streak: Int = 0,
    val completedThisWeek: Int = 0,
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startSession: StartFocusSessionUseCase,
    private val endSession: EndFocusSessionUseCase,
    observeActive: ObserveActiveFocusUseCase,
    observeRules: ObserveAppRulesUseCase,
    observeStats: ObserveFocusStatsUseCase,
    private val prefs: PreferencesRepository,
) : ViewModel() {

    val state: StateFlow<FocusUiState> = combine(
        observeActive(),
        observeRules(),
        observeStats.completedThisWeek(),
        prefs.preferences,
    ) { active, rules, completed, p ->
        FocusUiState(
            active = active,
            rules = rules,
            streak = p.focusStreak,
            completedThisWeek = completed,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusUiState())

    fun start(mode: FocusMode, durationMinutes: Int) {
        viewModelScope.launch {
            val blocked = state.value.rules.map { it.packageName }
            startSession(mode, TimeUnit.MINUTES.toMillis(durationMinutes.toLong()), blocked)
            FocusSessionService.start(context)
        }
    }

    fun stop() {
        viewModelScope.launch {
            val active = state.value.active ?: return@launch
            val completed = active.remainingMs <= 0
            endSession(active.id, completed)
            FocusSessionService.stop(context)
        }
    }
}
