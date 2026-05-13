package com.ksp.screentimereducer.domain.usecase

import com.ksp.screentimereducer.domain.model.AppUsage
import com.ksp.screentimereducer.domain.model.DailyTotals
import com.ksp.screentimereducer.domain.model.HourBucket
import com.ksp.screentimereducer.domain.model.WeeklyPoint
import com.ksp.screentimereducer.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncUsageUseCase @Inject constructor(
    private val repo: UsageRepository,
) {
    suspend operator fun invoke() = repo.syncToday()
}

class ObserveTodayUseCase @Inject constructor(
    private val repo: UsageRepository,
) {
    operator fun invoke(): Flow<DailyTotals> = repo.observeToday()
}

class ObserveWeeklyTrendUseCase @Inject constructor(
    private val repo: UsageRepository,
) {
    operator fun invoke(): Flow<List<WeeklyPoint>> = repo.observeWeekly()
}

class ObserveHourlyUseCase @Inject constructor(
    private val repo: UsageRepository,
) {
    operator fun invoke(): Flow<List<HourBucket>> = repo.observeHourly()
}

class ObserveTopAppsUseCase @Inject constructor(
    private val repo: UsageRepository,
) {
    operator fun invoke(limit: Int = 5): Flow<List<AppUsage>> = repo.observeTopApps(limit)
}
