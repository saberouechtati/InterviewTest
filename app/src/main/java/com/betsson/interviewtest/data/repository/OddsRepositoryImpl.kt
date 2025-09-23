package com.betsson.interviewtest.data.repository

import com.betsson.interviewtest.Bet // the existing Bet class
import com.betsson.interviewtest.data.logic.OddsLogicProcessor
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.repository.OddsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Locale.getDefault
import java.util.UUID // For generating unique IDs if needed

class OddsRepositoryImpl(
    private val oddsLogicProcessor: OddsLogicProcessor,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OddsRepository {

    private val _oddsDataFlow = MutableStateFlow<List<Odd>>(emptyList())

    init {
        loadInitialData()
    }

    // Adapted from MainActivity's getItemsFromNetwork
    private fun getItemsFromDataSource(): List<Bet> {
        // This is the hardcoded list from MainActivity
        // In a real app, this would be a network call
        return arrayListOf(
            Bet("Winning team", 10, 20, "https://i.imgur.com/mx66SBD.jpeg"),
            Bet("Total score", 2, 0, "https://i.imgur.com/VnPRqcv.jpeg"),
            Bet("Player performance", 5, 7, "https://i.imgur.com/Urpc00H.jpeg"),
            Bet("First goal scorer", 0, 80, "https://i.imgur.com/Wy94Tt7.jpeg"),
            Bet("Number of fouls", 5, 49, "https://i.imgur.com/NMLpcKj.jpeg"),
            Bet("Corner kicks", 3, 6, "https://i.imgur.com/TiJ8y5l.jpeg")
        )
    }

    private fun mapBetToOdd(bet: Bet): Odd {
        return Odd(
            // Using a combination of type and initial sellIn/odds for a more unique ID,
            // or generate a UUID if type alone isn't guaranteed unique.
            // For simplicity now, let's use type, but acknowledge this might need improvement.
            id = bet.type.replace(" ", "_").lowercase(getDefault()) + "_${UUID.randomUUID()}", // More robust ID
            name = bet.type,
            sellIn = bet.sellIn,
            oddsValue = bet.odds,
            imageUrl = bet.image
        )
    }

    private fun loadInitialData() {
        val betsFromSource = getItemsFromDataSource()
        val initialDomainOdds = betsFromSource.map { mapBetToOdd(it) }
        // Initial sort by sellIn as per requirement
        _oddsDataFlow.value = initialDomainOdds.sortedBy { it.sellIn }
    }

    override fun getOddsStream(): Flow<List<Odd>> {
        return _oddsDataFlow.asStateFlow()
    }

    override suspend fun triggerOddsUpdate() {
        withContext(ioDispatcher) {
            // Simulate some processing time if this were a longer operation
            // delay(100)

            val currentDomainOdds = _oddsDataFlow.value
            if (currentDomainOdds.isEmpty()) {
                // This case should ideally not happen if initial load works
                return@withContext
            }

            val updatedDomainOdds = oddsLogicProcessor.processOddsUpdate(currentDomainOdds)

            // Update the flow, ensuring it's sorted by sellIn
            _oddsDataFlow.update { updatedDomainOdds.sortedBy { it.sellIn } }
        }
    }
}
