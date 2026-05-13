package com.ksp.screentimereducer.data.repository

import com.ksp.screentimereducer.data.local.dao.ChallengeDao
import com.ksp.screentimereducer.data.local.entity.ChallengeEntity
import com.ksp.screentimereducer.domain.model.Challenge
import com.ksp.screentimereducer.domain.repository.ChallengeRepository
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepositoryImpl @Inject constructor(
    private val dao: ChallengeDao,
    private val prefs: PreferencesRepository,
) : ChallengeRepository {

    override suspend fun seedDefaults() {
        DEFAULTS.forEach { defaults ->
            if (dao.byId(defaults.id) == null) dao.upsert(defaults)
        }
    }

    override fun observeAll(): Flow<List<Challenge>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeActive(): Flow<List<Challenge>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override suspend fun start(id: String) {
        val c = dao.byId(id) ?: return
        dao.upsert(c.copy(active = true, startedAt = System.currentTimeMillis(), completedAt = null))
    }

    override suspend fun complete(id: String): Int {
        val c = dao.byId(id) ?: return 0
        dao.upsert(
            c.copy(
                active = false,
                completedAt = System.currentTimeMillis(),
                streak = c.streak + 1,
            )
        )
        prefs.addXp(c.xpReward)
        return c.xpReward
    }

    override suspend fun abandon(id: String) {
        val c = dao.byId(id) ?: return
        dao.upsert(c.copy(active = false, startedAt = null, completedAt = null))
    }

    private fun ChallengeEntity.toDomain() = Challenge(
        id = id,
        title = title,
        description = description,
        xpReward = xpReward,
        durationMs = durationMs,
        targetPackages = targetPackages.split(',').filter { it.isNotBlank() },
        startedAt = startedAt,
        completedAt = completedAt,
        streak = streak,
        active = active,
    )

    private companion object {
        val DEFAULTS = listOf(
            ChallengeEntity(
                id = "no_social_2h",
                title = "Social-free 2 hours",
                description = "Avoid social apps for 2 hours. Build momentum.",
                xpReward = 100,
                durationMs = TimeUnit.HOURS.toMillis(2),
                targetPackages = "com.instagram.android,com.zhiliaoapp.musically,com.facebook.katana,com.twitter.android,com.snapchat.android",
                startedAt = null,
                completedAt = null,
            ),
            ChallengeEntity(
                id = "no_shorts",
                title = "No shorts / reels",
                description = "Skip short-form video today.",
                xpReward = 150,
                durationMs = TimeUnit.HOURS.toMillis(12),
                targetPackages = "com.zhiliaoapp.musically,com.instagram.android,com.google.android.youtube",
                startedAt = null,
                completedAt = null,
            ),
            ChallengeEntity(
                id = "phone_free_sleep",
                title = "Phone-free sleep",
                description = "Don't unlock your phone between 10pm and 6am.",
                xpReward = 200,
                durationMs = TimeUnit.HOURS.toMillis(8),
                targetPackages = "",
                startedAt = null,
                completedAt = null,
            ),
            ChallengeEntity(
                id = "weekend_detox",
                title = "Weekend detox",
                description = "Half your usual screen time over a weekend.",
                xpReward = 500,
                durationMs = TimeUnit.DAYS.toMillis(2),
                targetPackages = "",
                startedAt = null,
                completedAt = null,
            ),
        )
    }
}
