package com.ksp.screentimereducer.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "focus_sessions",
    indices = [Index("startTime")]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String, // WORK, STUDY, SLEEP, CUSTOM
    val startTime: Long,
    val plannedDurationMs: Long,
    val endTime: Long?, // null while active
    val completed: Boolean,
    val blockedPackages: String // comma-separated; small enough to avoid a join table
)
