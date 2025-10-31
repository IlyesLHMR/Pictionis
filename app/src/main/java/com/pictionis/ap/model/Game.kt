package com.pictionis.ap.model

data class Game(
    val id: String = "",
    val hostId: String = "",
    val players: List<String> = emptyList(),
    val started: Boolean = false
)