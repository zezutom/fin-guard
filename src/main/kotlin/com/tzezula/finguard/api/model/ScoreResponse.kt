package com.tzezula.finguard.api.model

data class ScoreResponse(
    val decision: String,
    val risk: Int,
    val reasons: List<Map<String, Any?>>,
    val modelVersion: String,
    val modelUpdatedAt: String,
)
