package com.betsson.interviewtest.presentation.oddslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsson.interviewtest.domain.usecase.GetSortedOddsStreamUseCase
import com.betsson.interviewtest.domain.usecase.TriggerOddsUpdateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OddsListViewModel(
    private val getSortedOddsStreamUseCase: GetSortedOddsStreamUseCase,
    private val triggerOddsUpdateUseCase: TriggerOddsUpdateUseCase
    // Dependencies will be provided by Koin later
) : ViewModel() {

    private val _uiState = MutableStateFlow(OddsListUiState(isLoading = true))
    val uiState: StateFlow<OddsListUiState> = _uiState.asStateFlow()

    init {
        observeOdds()
    }

    private fun observeOdds() {
        getSortedOddsStreamUseCase() // Invoke the use case to get the flow
            .onEach { domainOddsList ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false, // Data received, no longer loading initial data
                        odds = domainOddsList.map { it.toOddItemUiModel() }, // Map to UI model
                        error = null // Clear any previous errors
                    )
                }
            }
            .catch { exception ->
                // Handle any errors from the flow
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "Error fetching odds: ${exception.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
            .launchIn(viewModelScope) // Collect the flow within the ViewModel's lifecycle scope
    }

    fun onUpdateOddsClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Indicate loading state for the update operation
            try {
                triggerOddsUpdateUseCase() // Invoke the use case
                // The observeOdds flow will automatically pick up the changes from the repository
                // and update the UI state. We just need to ensure isLoading is handled.
                // If the update itself doesn't cause an immediate emission that sets isLoading to false,
                // we might need to explicitly set it to false after a short delay or upon next emission.
                // However, since onEach in observeOdds sets isLoading = false, this should be fine.
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false, // Stop loading on error
                        error = "Failed to update odds: ${exception.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
            // No need to set isLoading to false here explicitly if observeOdds handles it
        }
    }
}

