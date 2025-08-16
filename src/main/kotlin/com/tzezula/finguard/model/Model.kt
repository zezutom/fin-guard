package com.tzezula.finguard.model

import java.time.Instant

data class Model(
    val version: String,
    val updatedAt: Instant,
    val binIndex: BinIndex,
    val mccRisk: MccRiskModel,
    val countryRisk: CountryRiskModel,
    val disposableDomains: Set<Domain>,
    val allow: AllowLists,
    val deny: DenyLists,
    val weights: Weights,
    val thresholds: Thresholds,
    val velocity: VelocityModel,
) {
    companion object {
        fun empty(): Model = Model(
            version = "empty",
            updatedAt = Instant.EPOCH,
            binIndex = BinIndex.empty(),
            mccRisk = emptyMap(),
            countryRisk = emptyMap(),
            disposableDomains = emptySet(),
            allow = AllowLists(),
            deny = DenyLists(),
            weights = emptyMap(),
            thresholds = Thresholds(),
            velocity = VelocityModel(),
        )
    }
}
