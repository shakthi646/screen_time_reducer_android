package com.ksp.screentimereducer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-app configuration that the user controls:
 * - delayEnabled: show the 5s mindful pause before launching
 * - delaySeconds: countdown duration (default 5)
 * - dailyLimitMs: optional daily usage cap (0 = no limit)
 */
@Entity(tableName = "app_rules")
data class AppRuleEntity(
    @PrimaryKey val packageName: String,
    val delayEnabled: Boolean = false,
    val delaySeconds: Int = 5,
    val dailyLimitMs: Long = 0L,
    val limitWarningShownAt: Long = 0L
)
