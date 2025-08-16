package com.tzezula.finguard.api

import com.tzezula.finguard.api.model.ModelResponse
import com.tzezula.finguard.api.model.toModelResponse
import com.tzezula.finguard.runtime.ModelHolder
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.coroutines.CoroutineContext

@RestController
@RequestMapping("/api/v1/model")
class ModelApi(
    private val modelHolder: ModelHolder,
    private val defaultContext: CoroutineContext,
) {

    @GetMapping
    suspend fun getModel(): ModelResponse = withContext(defaultContext) {
        modelHolder.current().toModelResponse()
    }
}
