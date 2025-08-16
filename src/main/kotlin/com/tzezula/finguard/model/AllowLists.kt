package com.tzezula.finguard.model

data class AllowLists(
    val merchants: Set<String> = emptySet(),
    val users: Set<String> = emptySet(),
    val cards: Set<String> = emptySet(),
)
