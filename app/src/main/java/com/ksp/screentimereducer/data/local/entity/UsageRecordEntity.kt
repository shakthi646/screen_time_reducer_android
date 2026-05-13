package com.ksp.screentimereducer.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per app per day. We aggregate raw UsageStatsManager events
 * down to this representation before persisting, which keeps the dashboard
 * queries snappy even when the user has years of history.
 */
@Entity(
    tableName = "usage_records",
    indices = [Index("date"), Index(value = ["date", "packageName"], unique = true)]
)
data class UsageRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val date: Long, // start-of-day timestamp (ms)
    val totalMs: Long,
    val launchCount: Int,
    val lastUpdated: Long
)
