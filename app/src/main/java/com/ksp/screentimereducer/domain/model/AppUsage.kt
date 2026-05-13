package com.ksp.screentimereducer.domain.model

data class AppUsage(
    val packageName: String,
    val label: String,
    val totalMs: Long,
    val launchCount: Int,
)

data class HourBucket(val hour: Int, val totalMs: Long)

data class DailyTotals(
    val totalScreenTimeMs: Long,
    val unlockCount: Int,
    val avgSessionMs: Long,
    val topApps: List<AppUsage>,
    val hourly: List<HourBucket>,
)

data class WeeklyPoint(val dayLabel: String, val totalMs: Long)
