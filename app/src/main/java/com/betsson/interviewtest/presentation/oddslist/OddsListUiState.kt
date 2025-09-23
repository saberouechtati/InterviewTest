package com.betsson.interviewtest.presentation.oddslist

import com.betsson.interviewtest.domain.model.Odd // For the mapper function

// Represents the overall state of the OddsList screen
data class OddsListUiState(
    val isLoading: Boolean = false,
    val odds: List<OddItemUiModel> = emptyList(),
    val error: String? = null // For displaying error messages
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
    return OddItemUiModel(
        id = this.id,
        name = this.name,
        sellInText = "Sell In: ${this.sellIn}",
        oddsValueText = "Odds: ${this.oddsValue}",
        imageUrl = this.imageUrl
    )
}

