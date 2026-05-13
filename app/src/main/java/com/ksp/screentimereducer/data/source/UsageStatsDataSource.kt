package com.ksp.screentimereducer.data.source

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.ksp.screentimereducer.core.time.DayBoundary
import com.ksp.screentimereducer.data.local.entity.UsageRecordEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads usage events from Android's UsageStatsManager and aggregates them
 * into per-app totals for the requested day.
 *
 * We use the event stream (not `queryUsageStats`) because the aggregate
 * stats can lag and don't give us launch counts. Walking events lets us
 * compute durations between MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND pairs.
 */
@Singleton
class UsageStatsDataSource @Inject constructor(
    private val context: Context,
) {

    fun aggregateForToday(): List<UsageRecordEntity> {
        val dayStart = DayBoundary.startOfToday()
        val now = System.currentTimeMillis()
        return aggregate(dayStart, now, dayStart)
    }

    /**
     * @param from inclusive window start
     * @param to inclusive window end
     * @param recordDate the canonical start-of-day timestamp to stamp on every row
     */
    fun aggregate(from: Long, to: Long, recordDate: Long): List<UsageRecordEntity> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val events = usm.queryEvents(from, to)
        val event = UsageEvents.Event()

        // packageName -> running totals
        data class Acc(var total: Long = 0, var launches: Int = 0, var lastFg: Long = -1)
        val map = HashMap<String, Acc>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val acc = map.getOrPut(pkg) { Acc() }
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    acc.lastFg = event.timeStamp
                    acc.launches += 1
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND,
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    if (acc.lastFg > 0) {
                        acc.total += (event.timeStamp - acc.lastFg).coerceAtLeast(0)
                        acc.lastFg = -1
                    }
                }
            }
        }

        // Close any still-foreground app against `to`
        for (acc in map.values) {
            if (acc.lastFg > 0) {
                acc.total += (to - acc.lastFg).coerceAtLeast(0)
                acc.lastFg = -1
            }
        }

        val now = System.currentTimeMillis()
        return map.entries
            .filter { it.value.total > 0 }
            .map { (pkg, acc) ->
                UsageRecordEntity(
                    packageName = pkg,
                    date = recordDate,
                    totalMs = acc.total,
                    launchCount = acc.launches,
                    lastUpdated = now,
                )
            }
    }
}
