package com.betsson.interviewtest.domain.model

data class Odd(
    val id: String,         // We'll generate this id or use 'type' if unique
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
    // Check if imageUrl is not null before trying to validate its content.
    // Then check if it's empty or doesn't start with "http".
    // The condition for require should be what we expect to be true for valid input.
    val imageUrlIsValid = this.imageUrl != null &&
            this.imageUrl.isNotEmpty() &&
            this.imageUrl.startsWith("http")

    require(imageUrlIsValid) {
        "Invalid image URL: '${this.imageUrl}'. URL must not be null, not empty, and must start with 'http'."
    }

    return OddItemUiModel(
        id = this.id,
        name = this.name,
        sellInText = "Sell In: ${this.sellIn}",
        oddsValueText = "Odds: ${this.oddsValue}",
        imageUrl = this.imageUrl // Safe due to the require block above ensuring it's not null and valid
    )
}