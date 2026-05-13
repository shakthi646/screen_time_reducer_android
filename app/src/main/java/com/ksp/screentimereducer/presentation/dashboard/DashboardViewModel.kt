package com.ksp.screentimereducer.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ksp.screentimereducer.domain.model.DailyTotals
import com.ksp.screentimereducer.domain.model.WeeklyPoint
import com.ksp.screentimereducer.domain.usecase.ObserveTodayUseCase
import com.ksp.screentimereducer.domain.usecase.ObserveWeeklyTrendUseCase
import com.ksp.screentimereducer.domain.usecase.SyncUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Empty(val message: String) : DashboardUiState
    data class Ready(
        val totals: DailyTotals,
        val weekly: List<WeeklyPoint>,
    ) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeToday: ObserveTodayUseCase,
    observeWeekly: ObserveWeeklyTrendUseCase,
    private val syncUsage: SyncUsageUseCase,
) : ViewModel() {

    val state: StateFlow<DashboardUiState> = combine(
        observeToday(),
        observeWeekly(),
    ) { today, weekly ->
        if (today.totalScreenTimeMs == 0L && today.topApps.isEmpty()) {
            DashboardUiState.Empty("No data yet — grant Usage Access to get started.")
        } else {
            DashboardUiState.Ready(totals = today, weekly = weekly)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { runCatching { syncUsage() } }
    }
}
