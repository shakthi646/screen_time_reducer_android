package com.ksp.screentimereducer.domain.model

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val durationMs: Long,
    val targetPackages: List<String>,
    val startedAt: Long?,
    val completedAt: Long?,
    val streak: Int,
    val active: Boolean,
) {
    val isActive: Boolean get() = active && startedAt != null && completedAt == null
    val progress: Float
        get() {
            val started = startedAt ?: return 0f
            val elapsed = System.currentTimeMillis() - started
            return (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
        }
}
