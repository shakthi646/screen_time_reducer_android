package com.ksp.screentimereducer.data.repository

import com.ksp.screentimereducer.core.time.DayBoundary
import com.ksp.screentimereducer.data.local.dao.FocusSessionDao
import com.ksp.screentimereducer.data.local.entity.FocusSessionEntity
import com.ksp.screentimereducer.domain.model.FocusMode
import com.ksp.screentimereducer.domain.model.FocusSession
import com.ksp.screentimereducer.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val dao: FocusSessionDao,
) : FocusRepository {

    override fun observeActive(): Flow<FocusSession?> =
        dao.observeActive().map { it?.toDomain() }

    override suspend fun activeOnce(): FocusSession? = dao.activeOnce()?.toDomain()

    override suspend fun start(mode: FocusMode, durationMs: Long, blockedPackages: List<String>): Long {
        val now = System.currentTimeMillis()
        val entity = FocusSessionEntity(
            mode = mode.name,
            startTime = now,
            plannedDurationMs = durationMs,
            endTime = null,
            completed = false,
            blockedPackages = blockedPackages.joinToString(","),
        )
        return dao.upsert(entity)
    }

    override suspend fun end(sessionId: Long, completed: Boolean) {
        val active = dao.activeOnce() ?: return
        if (active.id != sessionId) return
        dao.upsert(active.copy(endTime = System.currentTimeMillis(), completed = completed))
    }

    override fun observeCompletedThisWeek(): Flow<Int> =
        dao.observeCompletedSince(DayBoundary.startOfWeek())

    override fun observeRecent(): Flow<List<FocusSession>> =
        dao.observeRecent().map { list -> list.map { it.toDomain() } }

    private fun FocusSessionEntity.toDomain() = FocusSession(
        id = id,
        mode = runCatching { FocusMode.valueOf(mode) }.getOrDefault(FocusMode.CUSTOM),
        startTime = startTime,
        plannedDurationMs = plannedDurationMs,
        endTime = endTime,
        completed = completed,
        blockedPackages = blockedPackages.split(',').filter { it.isNotBlank() },
    )
}
