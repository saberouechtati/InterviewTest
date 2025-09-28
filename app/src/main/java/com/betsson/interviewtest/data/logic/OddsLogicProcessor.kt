package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.domain.model.Odd
import jakarta.inject.Inject


class OddsLogicProcessor @Inject constructor(
    private val strategyFactory: OddUpdateStrategyFactory
) {

    // This function directly adapts the logic from MainActivity.calculateOdds()
    // to work with a list of our domain.model.Odd objects.
    fun processOddsUpdate(oddsList: List<Odd>): List<Odd> {
        return oddsList.map { currentOdd ->
            val strategy = strategyFactory.createStrategy(currentOdd)
            strategy.update(currentOdd)
        }
    }
}
