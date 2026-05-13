package com.ksp.screentimereducer.domain.usecase

import com.ksp.screentimereducer.domain.model.AppRule
import com.ksp.screentimereducer.domain.model.InstalledApp
import com.ksp.screentimereducer.domain.repository.AppRuleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppRulesUseCase @Inject constructor(private val repo: AppRuleRepository) {
    operator fun invoke(): Flow<List<AppRule>> = repo.observeAll()
}

class ObserveDelayedAppsUseCase @Inject constructor(private val repo: AppRuleRepository) {
    operator fun invoke(): Flow<List<AppRule>> = repo.observeDelayed()
}

class ObserveLimitedAppsUseCase @Inject constructor(private val repo: AppRuleRepository) {
    operator fun invoke(): Flow<List<AppRule>> = repo.observeLimited()
}

class UpsertAppRuleUseCase @Inject constructor(private val repo: AppRuleRepository) {
    suspend operator fun invoke(rule: AppRule) = repo.upsert(rule)
}

class GetInstalledAppsUseCase @Inject constructor(private val repo: AppRuleRepository) {
    suspend operator fun invoke(includeSystem: Boolean = false): List<InstalledApp> =
        repo.installedApps().filter { includeSystem || !it.isSystem }
}
