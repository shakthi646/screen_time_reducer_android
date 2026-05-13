package com.ksp.screentimereducer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val durationMs: Long,
    val targetPackages: String, // comma-separated, optional
    val startedAt: Long?,
    val completedAt: Long?,
    val streak: Int = 0,
    val active: Boolean = false
)
