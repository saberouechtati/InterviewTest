package com.betsson.interviewtest.presentation.features.oddsList

import com.betsson.interviewtest.domain.model.OddItemUiModel

// Represents the overall state of the OddsList screen
data class OddsListUiState(
    val isLoading: Boolean = false,
    val odds: List<OddItemUiModel> = emptyList(),
    val error: String? = null // For displaying error messages
)

