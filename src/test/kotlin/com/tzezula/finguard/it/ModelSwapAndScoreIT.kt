package com.tzezula.finguard.it

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModelSwapAndScoreIT : BaseIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `swap to v2 via Kafka, then score using new model`() {
        // Verify that the current model is v1
        client.get().uri(url(port, "api/v1/model"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.version").isEqualTo("2025-08-16T18:00:00Z#v1")

        // Publish model update event to Kafka
        val payload = objectMapper.writeValueAsString(
            mapOf(
                "filename" to "model-v2.json",
                "createdAt" to "2025-08-16T18:05:00Z"
            )
        )
        kafkaTemplate().send("model-updates", payload)

        // Wait for the model to be swapped to v2
        await().atMost(10, TimeUnit.SECONDS).pollInterval(200, TimeUnit.MILLISECONDS).untilAsserted {
            client.get().uri(url(port, "api/v1/model"))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.version").isEqualTo("2025-08-16T18:05:00Z#v2")
        }

        // Score using the new model
        val scoreReq = """
            {
              "amount": 19.99,
              "currency": "GBP",
              "timestamp": "2025-08-16T18:06:00Z",
              "ip":"198.51.100.24",
              "ipCountry":"CZ",
              "deviceId":"dev:it",
              "userId":"usr:it",
              "emailHash":"abc123",
              "cardBin":"421234",
              "merchantId":"MRC123",
              "mcc":"7995",
              "merchantCountry":"GB",
              "userCountry":"GB"
            }
        """.trimIndent()

        // Verify the scoring response
        client.post().uri(url(port, "api/v1/score"))
            .header("Content-Type", "application/json")
            .bodyValue(scoreReq)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.modelVersion").isEqualTo("2025-08-16T18:05:00Z#v2")
            .jsonPath("$.modelUpdatedAt").isEqualTo("2025-08-16T18:05:00Z")
            .jsonPath("$.reasons[?(@.code=='BIN_COUNTRY_MISMATCH')]").exists()
            .jsonPath("$.risk").isEqualTo(12)
            .jsonPath("$.decision").isEqualTo("Accept")

    }
}
