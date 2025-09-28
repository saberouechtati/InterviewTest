package com.betsson.interviewtest.data.logic.strategies

import com.betsson.interviewtest.data.logic.LogicConstants
import com.betsson.interviewtest.data.logic.OddUpdateStrategy
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType
import org.junit.Assert.assertEquals
import org.junit.Test

class TotalScoreStrategyTest {

    private val strategy: OddUpdateStrategy = TotalScoreStrategy()

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
    fun `update - sellIn decreases by 1, oddsValue increases by SPECIAL_ODDS_INCREASE`() {
        val inputOdd = createOdd(name = OddType.TotalScore.displayName, sellIn = 10, oddsValue = 20)
        val result = strategy.update(inputOdd)

        assertEquals(9, result.sellIn)
        assertEquals(20 + LogicConstants.SPECIAL_ODDS_INCREASE, result.oddsValue)
    }

    @Test
    fun `update - oddsValue does not exceed MAX_ODDS_VALUE`() {
        val inputOdd = createOdd(
            name = OddType.TotalScore.displayName,
            sellIn = 10,
            oddsValue = LogicConstants.MAX_ODDS_VALUE
        )
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.MAX_ODDS_VALUE, result.oddsValue)
        assertEquals(9, result.sellIn)
    }

    @Test
    fun `update - oddsValue increases towards MAX_ODDS_VALUE`() {
        val inputOdd = createOdd(
            name = OddType.TotalScore.displayName,
            sellIn = 10,
            oddsValue = LogicConstants.MAX_ODDS_VALUE - LogicConstants.SPECIAL_ODDS_INCREASE
        )
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.MAX_ODDS_VALUE, result.oddsValue)
        assertEquals(9, result.sellIn)
    }

    @Test
    fun `update - when sellIn passed - oddsValue increases by SPECIAL_ODDS_INCREASE then by TOTAL_SCORE_ODDS_INCREASE_WHEN_SELL_IN_PASSED`() {
        val initialOdds = 20
        val inputOdd = createOdd(
            name = OddType.TotalScore.displayName,
            sellIn = LogicConstants.SELL_IN_PASSED_THRESHOLD -1, // e.g., -1
            oddsValue = initialOdds
        )
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.SELL_IN_PASSED_THRESHOLD - 2, result.sellIn)
        // initial -> initial + SPECIAL (normal) -> initial + SPECIAL + TOTAL_SCORE_WHEN_PASSED (sellIn passed)
        val expectedOdds = initialOdds + LogicConstants.SPECIAL_ODDS_INCREASE + LogicConstants.TOTAL_SCORE_ODDS_INCREASE_WHEN_SELL_IN_PASSED
        assertEquals(expectedOdds.coerceAtMost(LogicConstants.MAX_ODDS_VALUE), result.oddsValue)
    }

    @Test
    fun `update - when sellIn passed and odds near max - caps at MAX_ODDS_VALUE`() {
        val inputOdd = createOdd(
            name = OddType.TotalScore.displayName,
            sellIn = LogicConstants.SELL_IN_PASSED_THRESHOLD -1,
            oddsValue = LogicConstants.MAX_ODDS_VALUE - 1 // e.g. 49
        )
        val result = strategy.update(inputOdd)

        val expectedAfterFirstIncrease = (LogicConstants.MAX_ODDS_VALUE - 1 + LogicConstants.SPECIAL_ODDS_INCREASE).coerceAtMost(LogicConstants.MAX_ODDS_VALUE)
        val finalExpectedOdds = (expectedAfterFirstIncrease + LogicConstants.TOTAL_SCORE_ODDS_INCREASE_WHEN_SELL_IN_PASSED).coerceAtMost(LogicConstants.MAX_ODDS_VALUE)

        assertEquals(finalExpectedOdds, result.oddsValue)
    }


    @Test
    fun `update - when sellIn becomes passed in this update - oddsValue increases by both amounts`() {
        val initialOdds = 20
        // SellIn is 0, so after STANDARD_SELL_IN_DECREASE it becomes -1 (passed)
        val inputOdd = createOdd(name = OddType.TotalScore.displayName, sellIn = LogicConstants.SELL_IN_PASSED_THRESHOLD, oddsValue = initialOdds)
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.SELL_IN_PASSED_THRESHOLD - 1, result.sellIn)
        val expectedOdds = initialOdds + LogicConstants.SPECIAL_ODDS_INCREASE + LogicConstants.TOTAL_SCORE_ODDS_INCREASE_WHEN_SELL_IN_PASSED
        assertEquals(expectedOdds.coerceAtMost(LogicConstants.MAX_ODDS_VALUE), result.oddsValue)
    }
}
