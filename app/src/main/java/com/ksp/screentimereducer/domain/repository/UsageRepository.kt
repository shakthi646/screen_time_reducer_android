package com.ksp.screentimereducer.domain.repository

import com.ksp.screentimereducer.domain.model.AppUsage
import com.ksp.screentimereducer.domain.model.DailyTotals
import com.ksp.screentimereducer.domain.model.HourBucket
import com.ksp.screentimereducer.domain.model.WeeklyPoint
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    /** Pulls fresh data from UsageStatsManager and persists into Room. */
    suspend fun syncToday()

    /** Today's aggregated dashboard view. */
    fun observeToday(): Flow<DailyTotals>

    fun observeWeekly(): Flow<List<WeeklyPoint>>

    fun observeHourly(): Flow<List<HourBucket>>

    fun observeTopApps(limit: Int = 5): Flow<List<AppUsage>>

    suspend fun usageForApp(packageName: String): Long
}
