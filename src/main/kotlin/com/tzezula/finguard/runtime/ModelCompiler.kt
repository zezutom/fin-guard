package com.tzezula.finguard.runtime

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.tzezula.finguard.model.*
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.coroutines.CoroutineContext

@Component
class ModelCompiler(
    private val mapper: ObjectMapper,
    private val ioContext: CoroutineContext,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    suspend fun compile(path: Path): Model? = withContext(ioContext) {
        try {
            val json = Files.readString(path)
            val root = mapper.readTree(json)

            // Basic fields
            val version = parseVersion(root)
            val updatedAt = parseCreatedAt(root)    // Use "createdAt" for the last update time

            // Build the BIN index
            val binIndex = BinIndex.from(parsePrefixToCountryMappings(root))

            // Risk models
            val mccRisk = parseMccRisk(root)
            val countryRisk = parseCountryRisk(root)

            // Allow and deny lists
            val listsNode = root["lists"]
            val allowLists = parseAllowLists(listsNode)
            val denyLists = parseDenyLists(listsNode)

            // Disposable email domains
            val disposableDomains = parseDisposableDomains(root)

            // Weights
            val weights = parseWeights(root)

            // Thresholds and velocity limits
            val thresholds = parseThresholds(root)
            val velocity = parseVelocity(root)

            Model(
                version = version ?: "unknown",
                updatedAt = updatedAt ?: Instant.EPOCH,
                binIndex = binIndex,
                mccRisk = mccRisk,
                countryRisk = countryRisk,
                disposableDomains = disposableDomains,
                allow = allowLists,
                deny = denyLists,
                weights = weights,
                thresholds = thresholds,
                velocity = velocity,
            )
        } catch (e: IOException) {
            logger.error("Failed to compile model from JSON: {}", e.message, e)
            null
        }
    }

    private fun parseVelocity(root: JsonNode?): VelocityModel = root?.get("velocity")?.let {
        mapper.convertValue(it, VelocityModel::class.java)
    } ?: VelocityModel()

    private fun parseThresholds(root: JsonNode?): Thresholds = root?.get("thresholds")?.let {
        mapper.convertValue(it, Thresholds::class.java)
    } ?: Thresholds()

    private fun parseWeights(root: JsonNode?): Map<String, Int> =
        root?.get("weights")?.let { mapper.convertValue(it, Weights::class.java) } ?: emptyMap()

    private fun parseDisposableDomains(root: JsonNode?): Set<String> =
        root?.get("disposableDomains")?.map { it.asText() }?.toSet() ?: emptySet()

    private fun parseAllowLists(listsNode: JsonNode?): AllowLists = listsNode?.get("allow")?.let { node ->
        AllowLists(
            merchants = node["merchantIds"]?.map { it.asText() }?.toSet() ?: emptySet(),
        )
    } ?: AllowLists()

    private fun parseCountryRisk(root: JsonNode): Map<String, String> =
        root["countryRisk"]?.let { mapper.convertValue(it, CountryRiskModel::class.java) } ?: emptyMap()

    private fun parseMccRisk(root: JsonNode): Map<String, String> =
        root["mccRisk"]?.let { mapper.convertValue(it, MccRiskModel::class.java) } ?: emptyMap()

    private fun parsePrefixToCountryMappings(root: JsonNode): Map<String, String> =
        root["binTable"]?.associate { node ->
            val prefix = node["prefix"].asText()
            val country = node["country"].asText()
            prefix to country
        } ?: emptyMap()

    private fun parseCreatedAt(root: JsonNode): Instant? = root["createdAt"]?.asText()?.let { Instant.parse(it) }

    private fun parseVersion(root: JsonNode): String? = root["version"]?.asText()

}

private fun parseDenyLists(listsNode: JsonNode?): DenyLists = listsNode?.get("deny")?.let { node ->
    DenyLists(
        ips = node["ips"]?.map { it.asText() }?.toSet() ?: emptySet(),
        devices = node["devices"]?.map { it.asText() }?.toSet() ?: emptySet(),
        merchants = node["merchantIds"]?.map { it.asText() }?.toSet() ?: emptySet(),
    )
} ?: DenyLists()
