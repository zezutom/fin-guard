package com.tzezula.finguard.api.model

data class ScoreRequest(
    val amount: Double,
    val currency: String,
    val timestamp: String,
    val ip: String?,
    val ipCountry: String?,
    val deviceId: String?,
    val userId: String?,
    val emailHash: String?, // Hashed email only!
    val cardBin: String?,
    val merchantId: String?,
    val mcc: String?,
    val merchantCountry: String?,
    val userCountry: String?,
)
