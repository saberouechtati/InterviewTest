package com.betsson.interviewtest.data.repository

import android.util.Log
import com.betsson.interviewtest.data.dto.Bet
import com.betsson.interviewtest.data.logic.OddsLogicProcessor
import com.betsson.interviewtest.di.IoDispatcher
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType
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

const val TAG_ODDS_REPOSITORY = "OddsRepoBetsson"

class OddsRepositoryImpl @Inject constructor(
    private val oddsLogicProcessor: OddsLogicProcessor,
    @IoDispatcher private val defaultDispatcher: CoroutineDispatcher
) : OddsRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
    private val _oddsDataFlow = MutableStateFlow<List<Odd>>(emptyList())

    init {
        repositoryScope.launch {
            try {
                loadInitialData()
            } catch (e: Exception) {
                Log.e(TAG_ODDS_REPOSITORY, "init: Error during initial data load", e)
                // Optionally emit an error state or rethrow if your design requires it
            }
        }
    }

    private suspend fun getItemsFromDataSource(): List<Bet> {
        return withContext(defaultDispatcher) {
            // Simulate network delay or heavy computation
            kotlinx.coroutines.delay(1000)
            val items = listOf(
                Bet(OddType.fromName("Winning team"), 10, 20, "https://i.imgur.com/mx66SBD.jpeg"),
                Bet(OddType.fromName("Total score"), 2, 0, "https://i.imgur.com/VnPRqcv.jpeg"),
                Bet(OddType.fromName("Player performance"), 5, 7, "https://i.imgur.com/Urpc00H.jpeg"),
                Bet(OddType.fromName("First goal scorer"), 0, 80, "https://i.imgur.com/Wy94Tt7.jpeg"),
                Bet(OddType.fromName("Number of fouls"), 5, 49, "https://i.imgur.com/NMLpcKj.jpeg"),
                Bet(OddType.fromName("Corner kicks"), 3, 6, "https://i.imgur.com/TiJ8y5l.jpeg")
            )
            items
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
                    Log.w(TAG_ODDS_REPOSITORY, "triggerOddsUpdate: Current odds list is empty, cannot update.")
                    return@withContext
                }

                val updatedDomainOdds = oddsLogicProcessor.processOddsUpdate(currentDomainOdds)

                _oddsDataFlow.update { updatedDomainOdds.sortedBy { it.sellIn } }
            } catch (e: Exception) {
                Log.e(TAG_ODDS_REPOSITORY, "triggerOddsUpdate: Error during odds update", e)
                // Consider how to propagate this error if needed, e.g., through a specific error flow or result type
            }
        }
    }
}
