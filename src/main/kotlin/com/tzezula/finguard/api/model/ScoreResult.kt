package com.tzezula.finguard.api.model

import java.time.Instant

sealed interface ScoreResult {
    data class Success(
        val decision: ScoreDecision,
        val risk: Int,
        val reasons: List<RiskReasons>,
        val modelVersion: String,
        val modelUpdatedAt: Instant,
    ) : ScoreResult

    data object Failure : ScoreResult
}
