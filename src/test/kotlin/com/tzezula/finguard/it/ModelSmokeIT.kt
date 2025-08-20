package com.tzezula.finguard.it

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModelSmokeIT : BaseIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `bootstraps v1 model`() {
        client.get().uri(url(port, "api/v1/model"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.version").isEqualTo("2025-08-16T18:00:00Z#v1")
            .jsonPath("$.updatedAt").isEqualTo("2025-08-16T18:00:00Z")
    }
}
