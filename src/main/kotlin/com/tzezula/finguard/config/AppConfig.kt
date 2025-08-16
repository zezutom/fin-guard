package com.tzezula.finguard.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import kotlin.coroutines.CoroutineContext

@Configuration
class AppConfig {

    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper().registerKotlinModule()
        .registerModule(JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Bean
    fun ioContext(): CoroutineContext {
        return Dispatchers.IO + MDCContext()
    }

    @Bean
    fun defaultScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Default + MDCContext())
    }

    @Bean
    fun clock(): Clock {
        return Clock.systemUTC()
    }
}
