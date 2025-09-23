package com.betsson.interviewtest.domain.model

data class Odd(
    val id: String,         // We'll generate this or use 'type' if unique
    val name: String,       // Corresponds to 'type' in Bet.kt
    var sellIn: Int,
    var oddsValue: Int,   // Corresponds to 'odds' in Bet.kt
    val imageUrl: String?   // Corresponds to 'image' in Bet.kt
)