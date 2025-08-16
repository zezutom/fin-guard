package com.tzezula.finguard.runtime

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class ModelUpdateConsumer(
    private val modelCompiler: ModelCompiler,
    private val modelHolder: ModelHolder,
    private val objectMapper: ObjectMapper,
    private val defaultScope: CoroutineScope,
    @Value("\${finguard.model.snapshotsDir}") private val snapshotsDir: String,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["\${finguard.kafka.topic}"],
        groupId = "fin-guard",
    )
    fun onMessage(payload: String) {
        val msg = kotlin.runCatching {
            objectMapper.readValue(payload, UpdateMessage::class.java)
        }.getOrNull()

        // Intentionally no error handling here, so that Kafka retries failed messages.
        val file = msg?.filename ?: return
        val path = Path.of(snapshotsDir).resolve(file).normalize()

        defaultScope.launch {
            try {
                val next = modelCompiler.compile(path) ?: return@launch
                val current = modelHolder.current()
                if (next.updatedAt.isAfter(current.updatedAt)) {
                    logger.debug("New model version found: {}", next.updatedAt)
                    modelHolder.swap(next)
                } else {
                    logger.debug(
                        "Ignoring model update for {}: older than current version {}",
                        next.version,
                        current.version
                    )
                }
            } catch (e: Exception) {
                logger.error("Failed to compile model from JSON at {}: {}", path, e.message, e)
            }
        }
    }

}

data class UpdateMessage(
    val filename: String,
    val createdAt: String,
)
