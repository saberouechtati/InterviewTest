package com.betsson.interviewtest.domain.usecase

import com.betsson.interviewtest.domain.repository.OddsRepository

/**
 * Use case for triggering an update of all current odds according to the
 * defined business rules (e.g., sellIn and oddsValue adjustments).
 * The repository is responsible for applying these rules and ensuring
 * the odds stream reflects the changes.
 */
class TriggerOddsUpdateUseCase(private val oddsRepository: OddsRepository) {
    suspend operator fun invoke() {
        // This use case delegates the actual update logic to the repository.
        // Its role is to provide a clean entry point from the presentation layer
        // for this specific business action.
        oddsRepository.triggerOddsUpdate()
    }
}