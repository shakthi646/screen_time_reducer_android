package com.ksp.screentimereducer.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ksp.screentimereducer.domain.repository.UsageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Pulls the latest usage events from the OS and persists per-app totals
 * for the current day. Runs every 15 minutes so the dashboard widget and
 * daily-limit checks see fresh data without us re-querying the OS on
 * every Compose recomposition.
 */
@HiltWorker
class UsageAggregationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val usageRepo: UsageRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = runCatching {
        usageRepo.syncToday()
        Result.success()
    }.getOrElse { Result.retry() }

    companion object {
        const val NAME = "usage_aggregation_worker"
    }
}
