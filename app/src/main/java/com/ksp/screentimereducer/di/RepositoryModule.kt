package com.ksp.screentimereducer.di

import com.ksp.screentimereducer.data.repository.AppRuleRepositoryImpl
import com.ksp.screentimereducer.data.repository.ChallengeRepositoryImpl
import com.ksp.screentimereducer.data.repository.FocusRepositoryImpl
import com.ksp.screentimereducer.data.repository.PreferencesRepositoryImpl
import com.ksp.screentimereducer.data.repository.UnlockRepositoryImpl
import com.ksp.screentimereducer.data.repository.UsageRepositoryImpl
import com.ksp.screentimereducer.domain.repository.AppRuleRepository
import com.ksp.screentimereducer.domain.repository.ChallengeRepository
import com.ksp.screentimereducer.domain.repository.FocusRepository
import com.ksp.screentimereducer.domain.repository.PreferencesRepository
import com.ksp.screentimereducer.domain.repository.UnlockRepository
import com.ksp.screentimereducer.domain.repository.UsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds abstract fun bindUsageRepo(impl: UsageRepositoryImpl): UsageRepository
    @Binds abstract fun bindUnlockRepo(impl: UnlockRepositoryImpl): UnlockRepository
    @Binds abstract fun bindAppRuleRepo(impl: AppRuleRepositoryImpl): AppRuleRepository
    @Binds abstract fun bindFocusRepo(impl: FocusRepositoryImpl): FocusRepository
    @Binds abstract fun bindChallengeRepo(impl: ChallengeRepositoryImpl): ChallengeRepository
    @Binds abstract fun bindPrefsRepo(impl: PreferencesRepositoryImpl): PreferencesRepository
}
