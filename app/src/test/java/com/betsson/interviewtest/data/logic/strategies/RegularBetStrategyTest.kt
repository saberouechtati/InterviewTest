package com.betsson.interviewtest.data.logic.strategies

import com.betsson.interviewtest.data.logic.LogicConstants
import com.betsson.interviewtest.domain.model.Odd
import org.junit.Assert.assertEquals
import org.junit.Test

class RegularBetStrategyTest {

    private val strategy = RegularBetStrategy()

    // Helper from your original test
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
    fun `update - standard odd - decreases sellIn and oddsValue by 1`() {
        val inputOdd = createOdd(name = "Standard Bet", sellIn = 10, oddsValue = 20)
        val result = strategy.update(inputOdd)

        assertEquals(9, result.sellIn)
        assertEquals(19, result.oddsValue)
    }

    @Test
    fun `update - standard odd - oddsValue does not go below MIN_ODDS_VALUE_THRESHOLD`() {
        val inputOdd = createOdd(name = "Standard Bet Low Odds", sellIn = 10, oddsValue = LogicConstants.MIN_ODDS_VALUE_THRESHOLD)
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.MIN_ODDS_VALUE_THRESHOLD, result.oddsValue)
        assertEquals(9, result.sellIn)
    }

    @Test
    fun `update - standard odd - when sellIn passed - oddsValue decreases twice as fast`() {
        // Initial decrease, then another because sellIn is passed
        val inputOdd = createOdd(name = "Standard Bet SellIn Passed", sellIn = LogicConstants.SELL_IN_PASSED_THRESHOLD -1 , oddsValue = 20)
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.SELL_IN_PASSED_THRESHOLD - 2, result.sellIn)
        // 20 -> 19 (standard decrease)
        // 19 -> 18 (additional decrease because sellIn < 0)
        assertEquals(18, result.oddsValue)
    }

    @Test
    fun `update - standard odd - when sellIn passed and odds low - oddsValue does not go below MIN_ODDS_VALUE_THRESHOLD`() {
        val inputOdd = createOdd(name = "Standard Bet SellIn Passed Low Odds", sellIn = LogicConstants.SELL_IN_PASSED_THRESHOLD -1, oddsValue = 1)
        val result = strategy.update(inputOdd)
        // 1 -> 0 (standard decrease)
        // 0 -> still 0 (additional decrease because sellIn < 0, hits min)
        assertEquals(LogicConstants.MIN_ODDS_VALUE_THRESHOLD, result.oddsValue)
    }

    @Test
    fun `update - standard odd - when sellIn is exactly at passed threshold - oddsValue decreases twice as fast`() {
        val inputOdd = createOdd(name = "Standard Bet SellIn At Threshold", sellIn = LogicConstants.SELL_IN_PASSED_THRESHOLD, oddsValue = 20)
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.SELL_IN_PASSED_THRESHOLD - 1, result.sellIn)
        // 20 -> 19 (standard decrease)
        // newSellIn becomes -1, so it is < 0.
        // 19 -> 18 (additional decrease because newSellIn < 0)
        assertEquals(18, result.oddsValue)
    }
}
