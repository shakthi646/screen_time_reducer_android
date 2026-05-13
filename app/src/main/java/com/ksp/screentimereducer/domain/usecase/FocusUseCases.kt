package com.ksp.screentimereducer.domain.usecase

import com.ksp.screentimereducer.core.time.DayBoundary
import com.ksp.screentimereducer.data.preferences.UserPreferences
import com.ksp.screentimereducer.domain.model.FocusMode
import com.ksp.screentimereducer.domain.model.FocusSession
import com.ksp.screentimereducer.domain.repository.FocusRepository
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class StartFocusSessionUseCase @Inject constructor(
    private val repo: FocusRepository,
) {
    suspend operator fun invoke(mode: FocusMode, durationMs: Long, blocked: List<String>): Long =
        repo.start(mode, durationMs, blocked)
}

class EndFocusSessionUseCase @Inject constructor(
    private val repo: FocusRepository,
    private val prefs: PreferencesRepository,
) {
    suspend operator fun invoke(sessionId: Long, completed: Boolean) {
        repo.end(sessionId, completed)
        if (completed) {
            val current: UserPreferences = prefs.preferences.first()
            val today = DayBoundary.startOfToday()
            val yesterday = today - 24L * 60 * 60 * 1000
            val newStreak = if (current.lastFocusCompletedDate >= yesterday) current.focusStreak + 1 else 1
            prefs.recordFocusCompleted(newStreak)
            prefs.addXp(25)
        }
    }
}

class ObserveActiveFocusUseCase @Inject constructor(
    private val repo: FocusRepository,
) {
    operator fun invoke(): Flow<FocusSession?> = repo.observeActive()
}

class ObserveFocusStatsUseCase @Inject constructor(
    private val repo: FocusRepository,
) {
    fun completedThisWeek(): Flow<Int> = repo.observeCompletedThisWeek()
}
