package com.tzezula.finguard.model

data class VelocityModel(
    // The number of transactions. Default - transaction count in the last 5 minutes, 1 hour, and 1 day
    val windows: List<String> = listOf("PT5M", "PT1H", "P1D"),
    val limits: VelocityLimits = emptyMap(),
    val caps: Weights = emptyMap(),
)
