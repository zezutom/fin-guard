package com.tzezula.finguard.api

import com.tzezula.finguard.api.model.ScoreRequest
import com.tzezula.finguard.api.model.ScoreResponse
import com.tzezula.finguard.api.model.ScoreResult
import com.tzezula.finguard.api.model.toScoreResponse
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.coroutines.CoroutineContext

@RestController
@RequestMapping("/api/v1/score")
class ScoreApi(
    private val defaultContext: CoroutineContext,
    private val scoringEngine: ScoringEngine,
) {


    @PostMapping
    suspend fun score(
        @RequestBody req: ScoreRequest,
    ): ResponseEntity<ScoreResponse> = withContext(defaultContext) {
        when (val result = scoringEngine.score(req)) {
            is ScoreResult.Success -> ResponseEntity.ok().body(result.toScoreResponse())
            is ScoreResult.Failure -> ResponseEntity.internalServerError().build()
        }
    }
}
