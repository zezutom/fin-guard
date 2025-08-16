package com.tzezula.finguard.api.model

import com.tzezula.finguard.model.Model

typealias RiskReasons = Map<String, Any>

fun ScoreResult.Success.toScoreResponse() = ScoreResponse(
    decision = decision.name,
    risk = risk,
    reasons = reasons,
    modelVersion = modelVersion,
    modelUpdatedAt = modelUpdatedAt.toString(),
)

fun Model.toModelResponse() = ModelResponse(
    version = version,
    updatedAt = updatedAt.toString(),
)
