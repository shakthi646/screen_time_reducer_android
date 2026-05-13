package com.ksp.screentimereducer.domain.model

data class AppRule(
    val packageName: String,
    val delayEnabled: Boolean,
    val delaySeconds: Int,
    val dailyLimitMs: Long,
) {
    val hasLimit: Boolean get() = dailyLimitMs > 0
}
