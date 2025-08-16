package com.tzezula.finguard.model

data class Thresholds(
    val accept: Int = 20,
    val review: Int = 50,
    val decline: Int = 80,
)
