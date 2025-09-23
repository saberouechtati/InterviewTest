package com.betsson.interviewtest.domain.usecase

import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.repository.OddsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving a reactive stream of odds, which are expected
 * to be sorted by sellIn by the repository.
 */
class GetSortedOddsStreamUseCase(private val oddsRepository: OddsRepository) {
    operator fun invoke(): Flow<List<Odd>> {
        // The OddsRepository is already responsible for providing the data sorted by sellIn.
        // This use case primarily serves to abstract the direct repository dependency
        // from the ViewModel and clearly define this specific business operation.
        return oddsRepository.getOddsStream()
    }
}