package com.tzezula.finguard.model

data class DenyLists(
    val ips: Set<String> = emptySet(),
    val devices: Set<String> = emptySet(),
    val merchants: Set<String> = emptySet(),
)
