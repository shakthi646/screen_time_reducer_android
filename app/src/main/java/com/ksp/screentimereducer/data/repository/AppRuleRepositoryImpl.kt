package com.ksp.screentimereducer.data.repository

import com.ksp.screentimereducer.data.local.dao.AppRuleDao
import com.ksp.screentimereducer.data.local.entity.AppRuleEntity
import com.ksp.screentimereducer.data.source.InstalledAppsDataSource
import com.ksp.screentimereducer.domain.model.AppRule
import com.ksp.screentimereducer.domain.model.InstalledApp
import com.ksp.screentimereducer.domain.repository.AppRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRuleRepositoryImpl @Inject constructor(
    private val dao: AppRuleDao,
    private val installedAppsSource: InstalledAppsDataSource,
) : AppRuleRepository {

    override fun observeAll(): Flow<List<AppRule>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeDelayed(): Flow<List<AppRule>> =
        dao.observeWithDelay().map { list -> list.map { it.toDomain() } }

    override fun observeLimited(): Flow<List<AppRule>> =
        dao.observeWithLimits().map { list -> list.map { it.toDomain() } }

    override suspend fun ruleFor(packageName: String): AppRule? =
        dao.forPackage(packageName)?.toDomain()

    override suspend fun upsert(rule: AppRule) {
        dao.upsert(rule.toEntity())
    }

    override suspend fun delete(packageName: String) {
        dao.delete(packageName)
    }

    override suspend fun installedApps(): List<InstalledApp> =
        installedAppsSource.launcherApps()

    private fun AppRuleEntity.toDomain() = AppRule(
        packageName = packageName,
        delayEnabled = delayEnabled,
        delaySeconds = delaySeconds,
        dailyLimitMs = dailyLimitMs,
    )

    private fun AppRule.toEntity() = AppRuleEntity(
        packageName = packageName,
        delayEnabled = delayEnabled,
        delaySeconds = delaySeconds,
        dailyLimitMs = dailyLimitMs,
    )
}
