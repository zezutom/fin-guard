package com.tzezula.finguard.api

import com.tzezula.finguard.api.model.ModelUpdateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/model/admin")
class ModelAdminApi(
    private val modelEngine: ModelEngine,
    private val defaultScope: CoroutineScope,
) {

    @PostMapping("/update")
    suspend fun updateModel(
        @RequestBody req: ModelUpdateRequest,
    ): ResponseEntity<Unit> {
        defaultScope.launch {
            // Use the provided coroutine scope to launch the updateModel operation
            modelEngine.updateModel(req)
        }
        // Instantly return a response to the client
        return ResponseEntity.ok().build()
    }
}
