package com.ksp.screentimereducer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ksp.screentimereducer.data.local.entity.UnlockEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {

    @Insert
    suspend fun insert(event: UnlockEventEntity)

    @Query("SELECT * FROM unlock_events WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp ASC")
    fun observeRange(from: Long, to: Long): Flow<List<UnlockEventEntity>>

    @Query("SELECT COUNT(*) FROM unlock_events WHERE timestamp BETWEEN :from AND :to")
    fun observeCount(from: Long, to: Long): Flow<Int>

    @Query(
        """
        SELECT hourBucket, COUNT(*) AS c FROM unlock_events
        WHERE timestamp BETWEEN :from AND :to
        GROUP BY hourBucket
        ORDER BY hourBucket ASC
        """
    )
    fun observeHourlyHistogram(from: Long, to: Long): Flow<List<HourBucketCount>>

    @Query("DELETE FROM unlock_events WHERE timestamp < :olderThan")
    suspend fun pruneOlderThan(olderThan: Long)

    data class HourBucketCount(val hourBucket: Int, val c: Int)
}
