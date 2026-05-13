package com.ksp.screentimereducer.domain.repository

import com.ksp.screentimereducer.domain.model.Challenge
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    /** Seeds the default catalogue on first run. Idempotent. */
    suspend fun seedDefaults()
    fun observeAll(): Flow<List<Challenge>>
    fun observeActive(): Flow<List<Challenge>>
    suspend fun start(id: String)
    suspend fun complete(id: String): Int /* xp earned */
    suspend fun abandon(id: String)
}
