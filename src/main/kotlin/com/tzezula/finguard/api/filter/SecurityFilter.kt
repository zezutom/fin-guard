package com.tzezula.finguard.api.filter

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange

@Component
class SecurityFilter(
    @Value("\${finguard.security.adminApiKey}") private val apiKey: String
) : CoWebFilter() {

    private val protectedPaths = listOf(
        "/api/v1/model/admin"
    )

    override suspend fun filter(
        exchange: ServerWebExchange,
        chain: CoWebFilterChain,
    ) {
        val path = exchange.request.path.value()
        if (protectedPaths.contains(path) && exchange.request.headers[API_KEY_HEADER]?.first() != apiKey) {
            exchange.response.statusCode = org.springframework.http.HttpStatus.UNAUTHORIZED
            return
        }
        return chain.filter(exchange)
    }

    companion object {
        const val API_KEY_HEADER = "X-API-KEY"
    }
}
