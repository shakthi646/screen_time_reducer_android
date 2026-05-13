package com.ksp.screentimereducer.domain.repository

import com.ksp.screentimereducer.domain.model.FocusMode
import com.ksp.screentimereducer.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    fun observeActive(): Flow<FocusSession?>
    suspend fun activeOnce(): FocusSession?
    suspend fun start(mode: FocusMode, durationMs: Long, blockedPackages: List<String>): Long
    suspend fun end(sessionId: Long, completed: Boolean)
    fun observeCompletedThisWeek(): Flow<Int>
    fun observeRecent(): Flow<List<FocusSession>>
}
