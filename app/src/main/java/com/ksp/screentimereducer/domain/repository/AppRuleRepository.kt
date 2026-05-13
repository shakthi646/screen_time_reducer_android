package com.ksp.screentimereducer.domain.repository

import com.ksp.screentimereducer.domain.model.AppRule
import com.ksp.screentimereducer.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

interface AppRuleRepository {
    fun observeAll(): Flow<List<AppRule>>
    fun observeDelayed(): Flow<List<AppRule>>
    fun observeLimited(): Flow<List<AppRule>>
    suspend fun ruleFor(packageName: String): AppRule?
    suspend fun upsert(rule: AppRule)
    suspend fun delete(packageName: String)

    /** Installed launcher apps the user can apply rules to. */
    suspend fun installedApps(): List<InstalledApp>
}
