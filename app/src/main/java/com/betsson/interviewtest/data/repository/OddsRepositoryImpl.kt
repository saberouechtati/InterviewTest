package com.betsson.interviewtest.data.repository

import android.util.Log
import com.betsson.interviewtest.data.dto.Bet
import com.betsson.interviewtest.data.logic.OddsLogicProcessor
import com.betsson.interviewtest.data.test.DataSource
import com.betsson.interviewtest.di.IoDispatcher
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.repository.OddsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class OddsRepositoryImpl @Inject constructor(
    private val oddsLogicProcessor: OddsLogicProcessor,
    @IoDispatcher private val defaultDispatcher: CoroutineDispatcher
) : OddsRepository {

    companion object {
        private const val TAG = "OddsRepositoryImpl" // Good practice for TAG
        private const val ERROR_MSG_INITIAL_LOAD = "Error during initial data load"
        private const val TRIGGER_ODDS_UPDATE_CURRENT_ODDS_LIST_IS_EMPTY_CANNOT_UPDATE =
            "triggerOddsUpdate: Current odds list is empty, cannot update."
        private const val TRIGGER_ODDS_UPDATE_ERROR_DURING_ODDS_UPDATE =
            "triggerOddsUpdate: Error during odds update"
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
    private val _oddsDataFlow = MutableStateFlow<List<Odd>>(emptyList())

    init {
        repositoryScope.launch {
            try {
                loadInitialData()
            } catch (e: Exception) {
                Log.e(TAG, "$ERROR_MSG_INITIAL_LOAD. Details: ${e.message}", e)
                // Optionally emit an error state or rethrow if your design requires it
            }
        }
    }

    private suspend fun getItemsFromDataSource(): List<Bet> {
        return withContext(defaultDispatcher) {
            // Simulate network delay or heavy computation
            kotlinx.coroutines.delay(1000)
            DataSource.createOddsList()
        }
    }

    private suspend fun loadInitialData() {
        val betsFromSource = getItemsFromDataSource()
        val initialDomainOdds = betsFromSource.map { it.mapBetToOdd() }
        _oddsDataFlow.value = initialDomainOdds.sortedBy { it.sellIn }
    }

    override fun getOddsStream(): Flow<List<Odd>> {
        return _oddsDataFlow.asStateFlow()
    }

    override suspend fun triggerOddsUpdate() {
        withContext(defaultDispatcher) {
            try {
                val currentDomainOdds = _oddsDataFlow.value
                if (currentDomainOdds.isEmpty()) {
                    Log.w(TAG, TRIGGER_ODDS_UPDATE_CURRENT_ODDS_LIST_IS_EMPTY_CANNOT_UPDATE)
                    return@withContext
                }

                val updatedDomainOdds = oddsLogicProcessor.processOddsUpdate(currentDomainOdds)

                _oddsDataFlow.update { updatedDomainOdds.sortedBy { it.sellIn } }
            } catch (e: Exception) {
                Log.e(TAG, TRIGGER_ODDS_UPDATE_ERROR_DURING_ODDS_UPDATE, e)
                // Consider how to propagate this error if needed, e.g., through a specific error flow or result type
            }
        }
    }
}
