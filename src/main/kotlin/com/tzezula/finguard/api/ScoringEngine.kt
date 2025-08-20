package com.tzezula.finguard.api

import com.tzezula.finguard.api.model.RiskReasons
import com.tzezula.finguard.api.model.ScoreDecision
import com.tzezula.finguard.api.model.ScoreRequest
import com.tzezula.finguard.api.model.ScoreResult
import com.tzezula.finguard.runtime.ModelHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ScoringEngine(
    private val modelHolder: ModelHolder,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Scores a transaction based on the provided request.
     *
     * @param req The score request containing transaction details.
     * @return A [ScoreResult.Success] indicating the decision, risk score, and reasons for the decision.
     * If the scoring fails, returns [ScoreResult.Failure].
     */
    suspend fun score(req: ScoreRequest): ScoreResult = try {
        val model = modelHolder.current()
        logger.debug("Handling a score request: {}", req)

        var score = 0
        val reasons = mutableListOf<RiskReasons>()

        req.userCountry?.let { country ->
            req.cardBin?.let { model.binIndex.lookup(it) }?.let { issuer ->
                if (issuer != country) {
                    logger.debug("BIN country mismatch: issuer={} userCountry={}", issuer, country)
                    val weight = model.weights[BIN_COUNTRY_MISMATCH_KEY] ?: BIN_COUNTRY_MISMATCH_DEFAULT_WEIGHT
                    reasons += mapOf(CODE_KEY to BIN_COUNTRY_MISMATCH_KEY)
                    score += weight
                }
            }
        }
        // TODO Add more scoring rules here

        val decision = when {
            score < model.thresholds.accept -> ScoreDecision.Accept
            score < model.thresholds.review -> ScoreDecision.Review
            else -> ScoreDecision.Decline
        }

        val result = ScoreResult.Success(
            decision = decision,
            risk = score,
            reasons = reasons.toList(),
            modelVersion = model.version,
            modelUpdatedAt = model.updatedAt,
        )
        logger.debug("Scoring result: {}", result)
        result
    } catch (e: Exception) {
        logger.error("Failed to score a transaction: {}", e.message, e)
        ScoreResult.Failure
    }

    companion object {
        // Universal code and weight keys
        const val CODE_KEY = "code"

        // Specific scoring keys
        const val BIN_COUNTRY_MISMATCH_DEFAULT_WEIGHT = 12
        const val BIN_COUNTRY_MISMATCH_KEY = "BIN_COUNTRY_MISMATCH"
    }
}
