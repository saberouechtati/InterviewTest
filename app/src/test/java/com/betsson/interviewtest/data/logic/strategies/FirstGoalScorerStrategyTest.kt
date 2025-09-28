package com.betsson.interviewtest.data.logic.strategies

import com.betsson.interviewtest.data.logic.LogicConstants
import com.betsson.interviewtest.data.logic.OddUpdateStrategy
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType
import org.junit.Assert.assertEquals
import org.junit.Test

class FirstGoalScorerStrategyTest {private val strategy: OddUpdateStrategy = FirstGoalScorerStrategy()

    private fun createOdd(
        name: String,
        sellIn: Int,
        oddsValue: Int,
        id: String = name + "_" + sellIn + "_" + oddsValue,
        imageUrl: String = ""
    ): Odd {
        return Odd(id = id, name = name, sellIn = sellIn, oddsValue = oddsValue, imageUrl = imageUrl)
    }

    @Test
    fun `update - sellIn and oddsValue DO NOT change`() {
        val initialSellIn = 10
        val initialOddsValue = 30
        val inputOdd = createOdd(
            name = OddType.FirstGoalScorer.displayName,
            sellIn = initialSellIn,
            oddsValue = initialOddsValue
        )
        val result = strategy.update(inputOdd)

        assertEquals("SellIn should not change for FirstGoalScorer", initialSellIn, result.sellIn)
        assertEquals(
            "OddsValue should not change for FirstGoalScorer",
            initialOddsValue,
            result.oddsValue
        )
    }

    @Test
    fun `update - when sellIn passed - sellIn and oddsValue STILL DO NOT change`() {
        val initialSellIn = -1
        val initialOddsValue = 30
        val inputOdd = createOdd(
            name = OddType.FirstGoalScorer.displayName,
            sellIn = initialSellIn,
            oddsValue = initialOddsValue
        )
        val result = strategy.update(inputOdd)

        assertEquals(
            "SellIn should remain as passed for FirstGoalScorer",
            initialSellIn,
            result.sellIn
        )
        assertEquals(
            "OddsValue should not change for FirstGoalScorer even when sellIn passed",
            initialOddsValue,
            result.oddsValue
        )
    }

    @Test
    fun `update - with zero oddsValue - value does not change`() {
        val initialSellIn = 5
        val initialOddsValue = 0 // Already at a boundary, but shouldn't matter as it doesn't change
        val inputOdd = createOdd(
            name = OddType.FirstGoalScorer.displayName,
            sellIn = initialSellIn,
            oddsValue = initialOddsValue
        )
        val result = strategy.update(inputOdd)

        assertEquals(initialSellIn, result.sellIn)
        assertEquals(initialOddsValue, result.oddsValue)
    }

    @Test
    fun `update - with max oddsValue - value does not change`() {
        val initialSellIn = 5
        val initialOddsValue = LogicConstants.MAX_ODDS_VALUE
        val inputOdd = createOdd(
            name = OddType.FirstGoalScorer.displayName,
            sellIn = initialSellIn,
            oddsValue = initialOddsValue
        )
        val result = strategy.update(inputOdd)

        assertEquals(initialSellIn, result.sellIn)
        assertEquals(initialOddsValue, result.oddsValue)
    }
}
