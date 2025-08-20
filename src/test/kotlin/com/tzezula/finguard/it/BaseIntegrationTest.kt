package com.tzezula.finguard.it

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.junit.jupiter.api.TestInstance
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.redpanda.RedpandaContainer
import org.testcontainers.utility.DockerImageName
import java.nio.file.Files
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {
    companion object {

        @JvmStatic
        protected val kafka = RedpandaContainer(
            DockerImageName.parse("docker.redpanda.com/redpandadata/redpanda:v23.3.12")
        ).apply { start() }

        init {
            Runtime.getRuntime().addShutdownHook(Thread {
                runCatching { kafka.stop() }
            })
            createTopics()
        }

        // Temp dir for model snapshots
        @JvmStatic
        protected val snapshotsDir: Path = Files.createTempDirectory("model-snapshots").also { dir ->
            // Copy test models into the temp directory
            listOf(
                "model-v1.json",
                "model-v2.json",
            ).forEach { modelFile ->
                val source = Path.of("src", "test", "resources", "models", modelFile)
                val target = dir.resolve(modelFile)
                Files.copy(source, target)
            }
            Runtime.getRuntime().addShutdownHook(Thread {
                runCatching { dir.toFile().deleteRecursively() }
            })
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProps(reg: DynamicPropertyRegistry) {
            val bs = kafka.bootstrapServers.removePrefix("PLAINTEXT://")

            // Register dynamic application properties for tests

            // Spring Boot autoconfig, so that the @KafkaListener works
            reg.add("spring.kafka.bootstrap-servers") { bs }           // Spring Kafka autoconfig (listeners)
            reg.add("spring.kafka.consumer.auto-offset-reset") { "earliest" } // consume msgs sent before listener starts
            reg.add("spring.kafka.consumer.key-deserializer") { "org.apache.kafka.common.serialization.StringDeserializer" }
            reg.add("spring.kafka.consumer.value-deserializer") { "org.apache.kafka.common.serialization.StringDeserializer" }

            // Finguard application properties
            reg.add("finguard.kafka.bootstrapServers") { bs }
            reg.add("finguard.kafka.topic") { "model-updates" }
            reg.add("finguard.model.snapshotsDir") { snapshotsDir.toAbsolutePath().toString() }
            reg.add("finguard.model.bootstrapFile") { "model-v1.json" }
            reg.add("finguard.security.adminApiKey") { "TEST_KEY" }

            // Speed tests up
            reg.add("spring.main.lazy-initialization") { "true" }
            reg.add("logging.level.org.apache.kafka") { "WARN" }
        }

        private fun createTopics() {
            val admin = AdminClient.create(
                mapOf(
                    org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
                )
            )
            try {
                admin.createTopics(
                    listOf(
                        NewTopic("model-updates", 1, 1),
                    )
                ).all().get()
            } finally {
                admin.close()
            }
        }
    }

    protected val objectMapper = jacksonObjectMapper()

    protected fun kafkaTemplate(): KafkaTemplate<String, String> {
        val props = mapOf(
            org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.bootstrapServers,
            org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to org.apache.kafka.common.serialization.StringSerializer::class.java,
            org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to org.apache.kafka.common.serialization.StringSerializer::class.java,
        )
        val pf: ProducerFactory<String, String> = DefaultKafkaProducerFactory(props)
        return KafkaTemplate(pf)
    }
}
