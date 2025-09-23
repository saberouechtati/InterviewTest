package com.betsson.interviewtest.presentation.features.oddsList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.betsson.interviewtest.domain.usecase.GetSortedOddsStreamUseCase
import com.betsson.interviewtest.domain.usecase.TriggerOddsUpdateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val TAG_ODDS_VIEW_MODEL = "OddsViewModelBetsson"

class OddsListViewModel(
    private val getSortedOddsStreamUseCase: GetSortedOddsStreamUseCase,
    private val triggerOddsUpdateUseCase: TriggerOddsUpdateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OddsListUiState())
    val uiState: StateFlow<OddsListUiState> = _uiState.asStateFlow()

    init {
        observeOdds()
    }

    private fun observeOdds() {
        viewModelScope.launch {
            try {
                getSortedOddsStreamUseCase()
                    .onStart {
                        _uiState.update { it.copy(isLoading = true, error = null) } // Set loading on start
                    }
                    .onEach { domainOdds ->
                        try {
                            val uiModels = domainOdds.map { it.toOddItemUiModel() }
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    odds = uiModels,
                                    error = null
                                )
                            }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(isLoading = false, error = "Error processing data: ${e.message}") }
                        }
                    }
                    .catch { exception ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                error = "Data stream error: ${exception.message}"
                            )
                        }
                    }
                    .collect {
                        // This terminal collector might not be strictly necessary if onEach is doing all the work,
                        // but it ensures the flow is actively collected.
                        Log.d(TAG_ODDS_VIEW_MODEL, "observeOdds.collect: Terminal collect (flow is active)")
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Critical ViewModel error: ${e.message}") }
            }
        }
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
