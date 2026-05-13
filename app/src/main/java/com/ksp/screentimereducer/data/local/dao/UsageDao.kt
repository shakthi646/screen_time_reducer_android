package com.ksp.screentimereducer.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ksp.screentimereducer.data.local.entity.UsageRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    @Upsert
    suspend fun upsert(records: List<UsageRecordEntity>)

    @Query("SELECT * FROM usage_records WHERE date = :dayStart ORDER BY totalMs DESC")
    fun observeForDay(dayStart: Long): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun observeRange(from: Long, to: Long): Flow<List<UsageRecordEntity>>

    @Query("SELECT COALESCE(SUM(totalMs), 0) FROM usage_records WHERE date = :dayStart")
    fun observeTotalForDay(dayStart: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(totalMs), 0) FROM usage_records WHERE date = :dayStart AND packageName = :pkg")
    suspend fun totalForApp(dayStart: Long, pkg: String): Long

    @Query("DELETE FROM usage_records WHERE date < :olderThan")
    suspend fun pruneOlderThan(olderThan: Long)
}
