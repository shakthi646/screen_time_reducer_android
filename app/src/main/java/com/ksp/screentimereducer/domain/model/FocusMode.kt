package com.ksp.screentimereducer.domain.model

enum class FocusMode { WORK, STUDY, SLEEP, CUSTOM }

data class FocusSession(
    val id: Long,
    val mode: FocusMode,
    val startTime: Long,
    val plannedDurationMs: Long,
    val endTime: Long?,
    val completed: Boolean,
    val blockedPackages: List<String>,
) {
    val isActive: Boolean get() = endTime == null
    val remainingMs: Long get() = (startTime + plannedDurationMs - System.currentTimeMillis()).coerceAtLeast(0L)
}
