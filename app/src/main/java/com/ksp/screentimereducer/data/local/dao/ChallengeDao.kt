package com.ksp.screentimereducer.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ksp.screentimereducer.data.local.entity.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {

    @Upsert
    suspend fun upsert(challenge: ChallengeEntity)

    @Query("SELECT * FROM challenges")
    fun observeAll(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE active = 1")
    fun observeActive(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun byId(id: String): ChallengeEntity?
}
