package com.ksp.screentimereducer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ksp.screentimereducer.data.local.dao.AppRuleDao
import com.ksp.screentimereducer.data.local.dao.ChallengeDao
import com.ksp.screentimereducer.data.local.dao.FocusSessionDao
import com.ksp.screentimereducer.data.local.dao.UnlockDao
import com.ksp.screentimereducer.data.local.dao.UsageDao
import com.ksp.screentimereducer.data.local.entity.AppRuleEntity
import com.ksp.screentimereducer.data.local.entity.ChallengeEntity
import com.ksp.screentimereducer.data.local.entity.FocusSessionEntity
import com.ksp.screentimereducer.data.local.entity.UnlockEventEntity
import com.ksp.screentimereducer.data.local.entity.UsageRecordEntity

@Database(
    entities = [
        UsageRecordEntity::class,
        UnlockEventEntity::class,
        FocusSessionEntity::class,
        AppRuleEntity::class,
        ChallengeEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class ScreenTimeDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
    abstract fun unlockDao(): UnlockDao
    abstract fun focusDao(): FocusSessionDao
    abstract fun appRuleDao(): AppRuleDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        const val NAME = "screen_time.db"
    }
}
