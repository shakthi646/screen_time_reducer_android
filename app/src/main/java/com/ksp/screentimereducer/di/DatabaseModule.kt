package com.ksp.screentimereducer.di

import android.content.Context
import androidx.room.Room
import com.ksp.screentimereducer.data.local.ScreenTimeDatabase
import com.ksp.screentimereducer.data.local.dao.AppRuleDao
import com.ksp.screentimereducer.data.local.dao.ChallengeDao
import com.ksp.screentimereducer.data.local.dao.FocusSessionDao
import com.ksp.screentimereducer.data.local.dao.UnlockDao
import com.ksp.screentimereducer.data.local.dao.UsageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScreenTimeDatabase =
        Room.databaseBuilder(context, ScreenTimeDatabase::class.java, ScreenTimeDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUsageDao(db: ScreenTimeDatabase): UsageDao = db.usageDao()
    @Provides fun provideUnlockDao(db: ScreenTimeDatabase): UnlockDao = db.unlockDao()
    @Provides fun provideFocusDao(db: ScreenTimeDatabase): FocusSessionDao = db.focusDao()
    @Provides fun provideAppRuleDao(db: ScreenTimeDatabase): AppRuleDao = db.appRuleDao()
    @Provides fun provideChallengeDao(db: ScreenTimeDatabase): ChallengeDao = db.challengeDao()
}
