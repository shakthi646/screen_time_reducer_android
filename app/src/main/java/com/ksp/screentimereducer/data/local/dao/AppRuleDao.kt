package com.ksp.screentimereducer.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ksp.screentimereducer.data.local.entity.AppRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRuleDao {

    @Upsert
    suspend fun upsert(rule: AppRuleEntity)

    @Query("SELECT * FROM app_rules ORDER BY packageName")
    fun observeAll(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE delayEnabled = 1")
    fun observeWithDelay(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE dailyLimitMs > 0")
    fun observeWithLimits(): Flow<List<AppRuleEntity>>

    @Query("SELECT * FROM app_rules WHERE packageName = :pkg")
    suspend fun forPackage(pkg: String): AppRuleEntity?

    @Query("DELETE FROM app_rules WHERE packageName = :pkg")
    suspend fun delete(pkg: String)
}
