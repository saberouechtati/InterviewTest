package com.betsson.interviewtest.presentation.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.betsson.interviewtest.domain.usecase.GetSortedOddsStreamUseCase
import com.betsson.interviewtest.domain.usecase.TriggerOddsUpdateUseCase
import com.betsson.interviewtest.presentation.features.oddsList.OddsListViewModel

class OddsListViewModelFactory(
    private val getSortedOddsStreamUseCase: GetSortedOddsStreamUseCase,
    private val triggerOddsUpdateUseCase: TriggerOddsUpdateUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OddsListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OddsListViewModel(getSortedOddsStreamUseCase, triggerOddsUpdateUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}