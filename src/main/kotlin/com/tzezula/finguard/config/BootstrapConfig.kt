package com.tzezula.finguard.config

import com.tzezula.finguard.model.Model
import com.tzezula.finguard.runtime.ModelCompiler
import com.tzezula.finguard.runtime.ModelHolder
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException
import kotlin.io.path.Path

@Configuration
class BootstrapConfig {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun initialModel(
        modelCompiler: ModelCompiler,
        modelHolder: ModelHolder,
        @Value("\${finguard.model.snapshotsDir}") snapshotsDir: String,
        @Value("\${finguard.model.bootstrapFile}") bootstrapFile: String,
    ): CommandLineRunner = CommandLineRunner {
        runBlocking {
            try {
                val path = Path("$snapshotsDir/$bootstrapFile")
                val model = modelCompiler.compile(path) ?: run {
                    logger.warn(
                        "Failed to compile initial model from snapshots directory: {}/{}",
                        snapshotsDir,
                        bootstrapFile
                    )
                    return@runBlocking
                }
                modelHolder.swap(model)
                logger.info("Bootstrapped model version: {}, updateAt: {}", model.version, model.updatedAt)
            } catch (e: IOException) {
                logger.error(
                    "Failed to load initial model from snapshots directory: {}/{}",
                    snapshotsDir,
                    bootstrapFile,
                    e
                )
                Model.empty()
            }
        }
    }
}
