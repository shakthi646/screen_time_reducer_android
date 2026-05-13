package com.ksp.screentimereducer.widget

import android.content.Context
import com.ksp.screentimereducer.domain.repository.FocusRepository
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import com.ksp.screentimereducer.domain.repository.UnlockRepository
import com.ksp.screentimereducer.domain.repository.UsageRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

object WidgetSnapshotLoader {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun usageRepository(): UsageRepository
        fun unlockRepository(): UnlockRepository
        fun focusRepository(): FocusRepository
        fun preferencesRepository(): PreferencesRepository
    }

    suspend fun loadSnapshot(context: Context): WidgetSnapshot {
        val entry = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java,
        )
        return runCatching {
            val totals = entry.usageRepository().observeToday().first()
            val unlocks = entry.unlockRepository().observeTodayCount().first()
            val active = entry.focusRepository().observeActive().first()
            val prefs = entry.preferencesRepository().preferences.first()
            WidgetSnapshot(
                totalMs = totals.totalScreenTimeMs,
                unlocks = unlocks,
                streak = prefs.focusStreak,
                focusActive = active != null,
                focusElapsedMs = active?.let { System.currentTimeMillis() - it.startTime } ?: 0L,
            )
        }.getOrDefault(WidgetSnapshot())
    }
}
