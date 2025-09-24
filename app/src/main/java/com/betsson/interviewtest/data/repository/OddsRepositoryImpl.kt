package com.betsson.interviewtest.data.repository

import android.util.Log
import com.betsson.interviewtest.data.dto.Bet
import com.betsson.interviewtest.data.logic.OddsLogicProcessor
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
import java.util.Locale
import java.util.UUID

const val TAG_ODDS_REPOSITORY = "OddsRepoBetsson" // Define a tag

class OddsRepositoryImpl @Inject constructor(
    private val oddsLogicProcessor: OddsLogicProcessor,
    @IoDispatcher private val defaultDispatcher: CoroutineDispatcher
) : OddsRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
    private val _oddsDataFlow = MutableStateFlow<List<Odd>>(emptyList())

    init {
        Log.d(TAG_ODDS_REPOSITORY, "init: Repository instance created")
        repositoryScope.launch {
            Log.d(TAG_ODDS_REPOSITORY, "init: Launching loadInitialData in repositoryScope")
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
            Log.d(TAG_ODDS_REPOSITORY, "getItemsFromDataSource: Fetching items...")
            // Simulate network delay or heavy computation
            // kotlinx.coroutines.delay(500)
            val items = listOf(
                Bet("Winning team", 10, 20, "https://i.imgur.com/mx66SBD.jpeg"),
                Bet("Total score", 2, 0, "https://i.imgur.com/VnPRqcv.jpeg"),
                Bet("Player performance", 5, 7, "https://i.imgur.com/Urpc00H.jpeg"),
                Bet("First goal scorer", 0, 80, "https://i.imgur.com/Wy94Tt7.jpeg"),
                Bet("Number of fouls", 5, 49, "https://i.imgur.com/NMLpcKj.jpeg"),
                Bet("Corner kicks", 3, 6, "https://i.imgur.com/TiJ8y5l.jpeg")
            )
            Log.d(TAG_ODDS_REPOSITORY, "getItemsFromDataSource: Fetched ${items.size} items")
            items
        }
    }

    private fun mapBetToOdd(bet: Bet): Odd {
        // Log sparingly here if called many times, or only if debugging mapping
        return Odd(
            id = bet.type.replace(" ", "_").lowercase(Locale.getDefault()) + "_${UUID.randomUUID()}",
            name = bet.type,
            sellIn = bet.sellIn,
            oddsValue = bet.odds,
            imageUrl = bet.image
        )
    }

    private suspend fun loadInitialData() {
        Log.d(TAG_ODDS_REPOSITORY, "loadInitialData: Starting")
        val betsFromSource = getItemsFromDataSource()
        val initialDomainOdds = betsFromSource.map { mapBetToOdd(it) }
        _oddsDataFlow.value = initialDomainOdds.sortedBy { it.sellIn }
        Log.d(TAG_ODDS_REPOSITORY, "loadInitialData: Loaded and emitted ${initialDomainOdds.size} odds, sorted.")
    }

    override fun getOddsStream(): Flow<List<Odd>> {
        Log.d(TAG_ODDS_REPOSITORY, "getOddsStream: Called")
        return _oddsDataFlow.asStateFlow()
    }

    override suspend fun triggerOddsUpdate() {
        withContext(defaultDispatcher) {
            Log.d(TAG_ODDS_REPOSITORY, "triggerOddsUpdate: Starting")
            try {
                val currentDomainOdds = _oddsDataFlow.value
                if (currentDomainOdds.isEmpty()) {
                    Log.w(TAG_ODDS_REPOSITORY, "triggerOddsUpdate: Current odds list is empty, cannot update.")
                    return@withContext
                }

                Log.d(TAG_ODDS_REPOSITORY, "triggerOddsUpdate: Processing ${currentDomainOdds.size} odds with OddsLogicProcessor")
                val updatedDomainOdds = oddsLogicProcessor.processOddsUpdate(currentDomainOdds)
                Log.d(TAG_ODDS_REPOSITORY, "triggerOddsUpdate: OddsLogicProcessor returned ${updatedDomainOdds.size} updated odds")

                _oddsDataFlow.update { updatedDomainOdds.sortedBy { it.sellIn } }
                Log.d(TAG_ODDS_REPOSITORY, "triggerOddsUpdate: OddsDataFlow updated and sorted.")
            } catch (e: Exception) {
                Log.e(TAG_ODDS_REPOSITORY, "triggerOddsUpdate: Error during odds update", e)
                // Consider how to propagate this error if needed, e.g., through a specific error flow or result type
            }
        }
    }
}
