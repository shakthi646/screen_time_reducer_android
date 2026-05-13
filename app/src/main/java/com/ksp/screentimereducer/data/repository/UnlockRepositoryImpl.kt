package com.ksp.screentimereducer.data.repository

import com.ksp.screentimereducer.core.time.DayBoundary
import com.ksp.screentimereducer.data.local.dao.UnlockDao
import com.ksp.screentimereducer.data.local.entity.UnlockEventEntity
import com.ksp.screentimereducer.domain.repository.UnlockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnlockRepositoryImpl @Inject constructor(
    private val dao: UnlockDao,
) : UnlockRepository {

    override suspend fun recordUnlock(timestamp: Long) {
        val hour = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).hour
        dao.insert(UnlockEventEntity(timestamp = timestamp, hourBucket = hour))
    }

    override fun observeTodayCount(): Flow<Int> =
        dao.observeCount(DayBoundary.startOfToday(), DayBoundary.endOfToday())

    override fun observeHourly(): Flow<List<Pair<Int, Int>>> =
        dao.observeHourlyHistogram(DayBoundary.startOfToday(), DayBoundary.endOfToday())
            .map { rows ->
                val byHour = rows.associate { it.hourBucket to it.c }
                (0..23).map { it to (byHour[it] ?: 0) }
            }

    override fun observePeakHour(): Flow<Int?> = observeHourly().map { points ->
        points.maxByOrNull { it.second }?.takeIf { it.second > 0 }?.first
    }

    override fun observeWeeklyAverage(): Flow<Int> {
        val from = DayBoundary.startOfWeek()
        val to = DayBoundary.endOfToday()
        return dao.observeCount(from, to).map { total ->
            val days = ((to - from) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
            total / days
        }
    }
}
