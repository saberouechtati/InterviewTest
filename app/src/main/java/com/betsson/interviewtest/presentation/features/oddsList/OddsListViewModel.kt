package com.betsson.interviewtest.presentation.features.oddsList


import android.util.Log
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
                println("VIEWMODEL_LIFECYCLE - onStart called. isLoading=true") // ADD LOG
                _uiState.update { it.copy(isLoading = true, error = null) } // EMISSION 1
            }
            .onEach { domainOdds -> // problematicDomainOdds is received here
                println("VIEWMODEL_LIFECYCLE - onEach called with data.") // ADD LOG
                try {
                    println("VIEWMODEL_LIFECYCLE - onEach: Attempting to map...") // ADD LOG
                    val uiModels = domainOdds.map { it.toOddItemUiModel() } // EXPECTED TO THROW EXCEPTION
                    println("VIEWMODEL_LIFECYCLE - onEach: Mapping SUCCESSFUL (problem!)") // ADD LOG - Should NOT see this
                    _uiState.update { currentState ->
                        currentState.copy(isLoading = false, odds = uiModels, error = null)
                    }
                } catch (e: Exception) { // Mapping exception SHOULD BE CAUGHT HERE
                    println("VIEWMODEL_LIFECYCLE - onEach: CATCH BLOCK ENTERED for mapping error! Message: ${e.message}") // ADD LOG
                    _uiState.update { currentState -> // POTENTIAL EMISSION 2
                        println("VIEWMODEL_LIFECYCLE - onEach CATCH: Updating state. isLoading=false, error=${e.message}") // ADD LOG
                        currentState.copy(
                            isLoading = false,
                            odds = emptyList(), // Or currentState.odds
                            error = "Error processing data: ${e.message}"
                        )
                    }
                }
            }
            .catch { exception -> // Outer catch
                println("VIEWMODEL_LIFECYCLE - OUTER CATCH BLOCK ENTERED! Message: ${exception.message}") // ADD LOG
                _uiState.update { currentState ->
                    currentState.copy(isLoading = false, error = "Data stream error: ${exception.message}")
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
                        error = "Update failed: ${e.message}"
                    )
                }
            }
        }
    }
}
