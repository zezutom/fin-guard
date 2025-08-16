package com.tzezula.finguard.model

interface BinIndex {
    /**
     * Looks up the BIN (Bank Identification Number) in the index.
     * @param bin The BIN to look up, which is typically the first 6 or 8 digits of a card number.
     * @return ISO 3166-1 alpha-2 issuer country or null if unknown.
     */
    fun lookup(bin: String): String?

    companion object {
        fun empty(): BinIndex = object : BinIndex {
            override fun lookup(bin: String): String? {
                return null
            }
        }
        fun from(prefixToCountry: Map<String, String>): BinIndex {
            if (prefixToCountry.isEmpty()) {
                return empty()
            }

            // Find the maximum prefix length
            val maxPrefix = prefixToCountry.keys.maxOf { it.length }

            // Create the index
            return RadixBinIndex(prefixToCountry, maxPrefix)
        }
    }
}

/**
 * Interface for a binary index that allows looking up a bin.
 */
private class RadixBinIndex(
    private val prefixToCountry: Map<String, String>,
    private val maxPrefix: Int
) : BinIndex {
    override fun lookup(bin: String): String? {
        val n = bin.length.coerceAtMost(maxPrefix)

        // Longest prefix match
        for (len in n downTo 1) {
            val prefix = bin.substring(0, len)
            prefixToCountry[prefix]?.let { return it }
        }

        // If no match found, return null
        return null
    }
}
