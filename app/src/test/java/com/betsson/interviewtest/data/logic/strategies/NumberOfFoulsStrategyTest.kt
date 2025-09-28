package com.betsson.interviewtest.data.logic.strategies

import com.betsson.interviewtest.data.logic.LogicConstants
import com.betsson.interviewtest.data.logic.OddUpdateStrategy
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberOfFoulsStrategyTest {

    private val strategy: OddUpdateStrategy = NumberOfFoulsStrategy()

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
    fun `update - sellIn high - oddsValue increases by SPECIAL_ODDS_INCREASE only`() {
        val inputOdd = createOdd(
            name = OddType.NumberOfFouls.displayName,
            sellIn = LogicConstants.FOULS_SELL_IN_THRESHOLD_PRIMARY + 1, // e.g., 12
            oddsValue = 20)
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.FOULS_SELL_IN_THRESHOLD_PRIMARY, result.sellIn) // 12 -> 11
        assertEquals(20 + LogicConstants.SPECIAL_ODDS_INCREASE, result.oddsValue)
    }

    @Test
    fun `update - sellIn just below primary threshold - odds increase by SPECIAL + BONUS`() {
        // sellIn = 10 (which is < FOULS_SELL_IN_THRESHOLD_PRIMARY = 11 but >= FOULS_SELL_IN_THRESHOLD_SECONDARY = 6)
        val inputOdd = createOdd(
            name = OddType.NumberOfFouls.displayName,
            sellIn = LogicConstants.FOULS_SELL_IN_THRESHOLD_PRIMARY - 1, // e.g. 10
            oddsValue = 20
        )
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.FOULS_SELL_IN_THRESHOLD_PRIMARY - 2, result.sellIn) // 10 -> 9
        val expectedOdds = 20 + LogicConstants.SPECIAL_ODDS_INCREASE + LogicConstants.FOULS_ODDS_BONUS_INCREASE
        assertEquals(expectedOdds.coerceAtMost(LogicConstants.MAX_ODDS_VALUE), result.oddsValue)
    }

    @Test
    fun `update - sellIn at primary threshold - odds increase by SPECIAL only`() { // Test name adjusted for clarity
        val inputOdd = createOdd(
            name = OddType.NumberOfFouls.displayName,
            sellIn = LogicConstants.FOULS_SELL_IN_THRESHOLD_PRIMARY, // sellIn = 11
            oddsValue = 20
        )
        val result = strategy.update(inputOdd) // sellIn becomes 10

        assertEquals(LogicConstants.FOULS_SELL_IN_THRESHOLD_PRIMARY - 1, result.sellIn) // 11 -> 10
        // Expected: 20 + SPECIAL_INCREASE(1) because 20 < 50
        //           NO FOULS_ODDS_BONUS_INCREASE because current sellIn (11) is NOT < FOULS_SELL_IN_THRESHOLD_PRIMARY (11)
        val expectedOdds = 20 + LogicConstants.SPECIAL_ODDS_INCREASE
        assertEquals("Odds should be 21 when initial sellIn is 11",
            expectedOdds.coerceAtMost(LogicConstants.MAX_ODDS_VALUE), result.oddsValue) // Should be 21
    }

    @Test
    fun `update - sellIn just below secondary threshold - odds increase by SPECIAL + DOUBLE BONUS`() {
        // sellIn = 5 (which is < FOULS_SELL_IN_THRESHOLD_SECONDARY = 6)
        val inputOdd = createOdd(
            name = OddType.NumberOfFouls.displayName,
            sellIn = LogicConstants.FOULS_SELL_IN_THRESHOLD_SECONDARY - 1, // e.g. 5
            oddsValue = 20
        )
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.FOULS_SELL_IN_THRESHOLD_SECONDARY - 2, result.sellIn) // 5 -> 4
        val expectedOdds = 20 + LogicConstants.SPECIAL_ODDS_INCREASE + LogicConstants.FOULS_ODDS_BONUS_INCREASE + LogicConstants.FOULS_ODDS_BONUS_INCREASE
        assertEquals(expectedOdds.coerceAtMost(LogicConstants.MAX_ODDS_VALUE), result.oddsValue)
    }

    @Test
    fun `update - sellIn at secondary threshold - becomes below after decrease - odds increase by SPECIAL + ONE BONUS`() { // Test name adjusted for clarity
        val inputOdd = createOdd(
            name = OddType.NumberOfFouls.displayName,
            sellIn = LogicConstants.FOULS_SELL_IN_THRESHOLD_SECONDARY, // sellIn = 6
            oddsValue = 20
        )
        val result = strategy.update(inputOdd) // sellIn becomes 5

        assertEquals(LogicConstants.FOULS_SELL_IN_THRESHOLD_SECONDARY - 1, result.sellIn) // 6 -> 5
        // Expected: 20 + SPECIAL_INCREASE(1) because 20 < 50
        //           + FOULS_ODDS_BONUS_INCREASE(1) because current sellIn (6) < FOULS_SELL_IN_THRESHOLD_PRIMARY (11)
        //           NO second FOULS_ODDS_BONUS_INCREASE because current sellIn (6) is NOT < FOULS_SELL_IN_THRESHOLD_SECONDARY (6)
        val expectedOdds = 20 + LogicConstants.SPECIAL_ODDS_INCREASE + LogicConstants.FOULS_ODDS_BONUS_INCREASE
        assertEquals("Odds should be 22 when initial sellIn is 6",
            expectedOdds.coerceAtMost(LogicConstants.MAX_ODDS_VALUE), result.oddsValue) // Should be 22
    }

    @Test
    fun `update - bonus increases do not exceed MAX_ODDS_VALUE`() {
        val inputOdd = createOdd(
            name = OddType.NumberOfFouls.displayName,
            sellIn = LogicConstants.FOULS_SELL_IN_THRESHOLD_SECONDARY - 1, // e.g. 5
            oddsValue = LogicConstants.MAX_ODDS_VALUE - 2 // e.g. 48
        )
        // Expected: 48 -> 48+S -> 48+S+B1 -> 48+S+B1+B2, capped at MAX_ODDS_VALUE
        val result = strategy.update(inputOdd)
        assertEquals(LogicConstants.MAX_ODDS_VALUE, result.oddsValue)
    }

    @Test
    fun `update - when sellIn passed - oddsValue becomes 0`() {
        val inputOdd = createOdd(
            name = OddType.NumberOfFouls.displayName,
            sellIn = LogicConstants.SELL_IN_PASSED_THRESHOLD -1, // e.g. -1
            oddsValue = 20
        )
        val result = strategy.update(inputOdd)

        assertEquals(LogicConstants.SELL_IN_PASSED_THRESHOLD - 2, result.sellIn)
        assertEquals(0, result.oddsValue)
    }

    @Test
    fun `update - when sellIn becomes passed in this update - oddsValue becomes 0`() {
        val inputOdd = createOdd(
            name = OddType.NumberOfFouls.displayName,
            sellIn = LogicConstants.SELL_IN_PASSED_THRESHOLD, // e.g. 0
            oddsValue = 20
        )
        val result = strategy.update(inputOdd) // sellIn becomes -1

        assertEquals(LogicConstants.SELL_IN_PASSED_THRESHOLD - 1, result.sellIn)
        assertEquals(0, result.oddsValue)
    }
}
