package com.ksp.screentimereducer.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ksp.screentimereducer.data.work.UsageAggregationWorker
import com.ksp.screentimereducer.data.work.WidgetRefreshWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Registers the recurring background jobs we rely on:
     * - usage aggregation (every 15 min, the WorkManager floor)
     * - widget refresh (every 30 min)
     *
     * Uses KEEP so we don't churn through job IDs on every app start.
     */
    fun scheduleRecurringJobs() {
        val wm = WorkManager.getInstance(context)
        val noConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        wm.enqueueUniquePeriodicWork(
            UsageAggregationWorker.NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<UsageAggregationWorker>(15, TimeUnit.MINUTES)
                .setConstraints(noConstraints)
                .build()
        )

        wm.enqueueUniquePeriodicWork(
            WidgetRefreshWorker.NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WidgetRefreshWorker>(30, TimeUnit.MINUTES)
                .setConstraints(noConstraints)
                .build()
        )
    }
}
