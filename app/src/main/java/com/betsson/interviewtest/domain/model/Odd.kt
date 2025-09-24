package com.betsson.interviewtest.domain.model

data class Odd(
    val id: String,         // We'll generate this or use 'type' if unique
    val name: String,       // Corresponds to 'type' in Bet.kt
    var sellIn: Int,
    var oddsValue: Int,   // Corresponds to 'odds' in Bet.kt
    val imageUrl: String?   // Corresponds to 'image' in Bet.kt
)

// Represents a single item as it will be displayed in the UI list
data class OddItemUiModel(
    val id: String,
    val name: String,
    val sellInText: String,    // Formatted for display
    val oddsValueText: String, // Formatted for display
    val imageUrl: String?
)

// Mapper function to convert a domain Odd model to a UI-friendly OddItemUiModel
fun Odd.toOddItemUiModel(): OddItemUiModel {
    if (this.imageUrl?.isEmpty() == true || this.imageUrl?.startsWith("http")?.not() == true) {
        throw IllegalArgumentException("Invalid image URL: ${this.imageUrl}")
    }
    return OddItemUiModel(
        id = this.id,
        name = this.name,
        sellInText = "Sell In: ${this.sellIn}",
        oddsValueText = "Odds: ${this.oddsValue}",
        imageUrl = this.imageUrl
    )
}