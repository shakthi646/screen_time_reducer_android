package com.ksp.screentimereducer.domain.model

data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
)
