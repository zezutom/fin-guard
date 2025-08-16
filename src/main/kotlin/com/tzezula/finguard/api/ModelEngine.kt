package com.tzezula.finguard.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.tzezula.finguard.api.model.ModelUpdateRequest
import com.tzezula.finguard.runtime.UpdateMessage
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Clock

@Component
class ModelEngine(
    private val kafka: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
    @Value("\${finguard.kafka.topic}") private val topic: String,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Updates the model by sending a message to the Kafka topic.
     *
     * @param req The request containing the filename of the model to update.
     */
    suspend fun updateModel(req: ModelUpdateRequest) {
        val payload = UpdateMessage(
            req.filename,
            clock.instant().toString()
        )
        val message = objectMapper.writeValueAsString(payload)
        logger.info("Sending model update message: {}", message)
        kafka.send(topic, message).await()
    }
}
