package com.ksp.screentimereducer.domain.repository

import kotlinx.coroutines.flow.Flow

interface UnlockRepository {
    suspend fun recordUnlock(timestamp: Long = System.currentTimeMillis())
    fun observeTodayCount(): Flow<Int>
    fun observeHourly(): Flow<List<Pair<Int, Int>>>
    fun observePeakHour(): Flow<Int?>
    fun observeWeeklyAverage(): Flow<Int>
}
