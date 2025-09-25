package com.betsson.interviewtest.presentation.features.oddsList


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsson.interviewtest.domain.model.toOddItemUiModel
import com.betsson.interviewtest.domain.usecase.GetSortedOddsStreamUseCase
import com.betsson.interviewtest.domain.usecase.TriggerOddsUpdateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DATA_STREAM_ERROR = "Data stream error"
private const val ERROR_PROCESSING_DATA = "Error processing data"
private const val UPDATE_FAILED = "Update failed"

@HiltViewModel
class OddsListViewModel @Inject constructor(
    private val getSortedOddsStreamUseCase: GetSortedOddsStreamUseCase,
    private val triggerOddsUpdateUseCase: TriggerOddsUpdateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OddsListUiState())
    val uiState: StateFlow<OddsListUiState> = _uiState.asStateFlow()

    init {
        observeOdds()
    }

    // Simplified observeOdds
    private fun observeOdds() {
        getSortedOddsStreamUseCase() // From test: flow { emit(problematicDomainOdds) }
            .onStart {
                _uiState.update { it.copy(isLoading = true, error = null) } // EMISSION 1
            }
            .onEach { domainOdds -> // problematicDomainOdds is received here
                try {
                    val uiModels =
                        domainOdds.map { it.toOddItemUiModel() } // EXPECTED TO THROW EXCEPTION
                    _uiState.update { currentState ->
                        currentState.copy(isLoading = false, odds = uiModels, error = null)
                    }
                } catch (e: Exception) { // Mapping exception SHOULD BE CAUGHT HERE
                    _uiState.update { currentState -> // POTENTIAL EMISSION 2
                        currentState.copy(
                            isLoading = false,
                            odds = emptyList(), // Or currentState.odds
                            error = "$ERROR_PROCESSING_DATA: ${e.message}"
                        )
                    }
                }
            }
            .catch { exception -> // Outer catch
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "$DATA_STREAM_ERROR: ${exception.message}"
                    )
                }
            }
            .launchIn(viewModelScope)
    }


    fun onUpdateOddsClicked() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                triggerOddsUpdateUseCase()
                // isLoading will be set to false by the observeOdds flow when new data arrives
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        error = "$UPDATE_FAILED: ${e.message}"
                    )
                }
            }
        }
    }
}
