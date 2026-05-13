package com.ksp.screentimereducer.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ksp.screentimereducer.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Upsert
    suspend fun upsert(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions WHERE endTime IS NULL LIMIT 1")
    fun observeActive(): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun activeOnce(): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC LIMIT :limit")
    fun observeRecent(limit: Int = 30): Flow<List<FocusSessionEntity>>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1 AND startTime >= :from")
    fun observeCompletedSince(from: Long): Flow<Int>
}
