package com.pictionis.ap.model

data class Game(
    val id: String = "",
    val hostId: String = "",
    val players: List<String> = emptyList(),
    val started: Boolean = false,
    val currentWord: String = "",        // Mot à deviner
    val drawerId: String = "",           // UID du joueur qui dessine
    val scores: Map<String, Int> = emptyMap(), // scores par joueur (UID -> points)
    val round: Int = 1,                  // numéro du tour
    val chat: List<ChatMessage> = emptyList()  // messages du chat (à définir)
)