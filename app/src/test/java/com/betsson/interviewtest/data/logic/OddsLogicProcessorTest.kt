package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType // Ensure this is the correct path to your OddType
import org.junit.Assert.assertEquals
import org.junit.Test // If using JUnit 4
// import org.junit.jupiter.api.Test // If using JUnit 5

class OddsLogicProcessorTest {

    private val processor = OddsLogicProcessor()

    // Helper function to create Odd instances easily in tests
    // Assumes Odd has id, name, sellIn, oddsValue, imageUrl properties. Adjust if different.
    private fun createOdd(
        name: String,
        sellIn: Int,
        oddsValue: Int,
        id: String = name + "_" + sellIn + "_" + oddsValue, // Simple unique ID for tests
        imageUrl: String = "" // Not used by processor logic, but required by Odd constructor
    ): Odd {
        return Odd(id = id, name = name, sellIn = sellIn, oddsValue = oddsValue, imageUrl = imageUrl)
    }

    // --- Standard Odds Tests (Not "Total Score", "Number of Fouls", or "First Goal Scorer") ---

    @Test
    fun `processOddsUpdate - standard odd - decreases sellIn and oddsValue by 1`() {
        val inputOdd = createOdd(name = "Standard Bet", sellIn = 10, oddsValue = 20)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(1, result.size)
        assertEquals(9, result[0].sellIn)
        assertEquals(19, result[0].oddsValue)
        assertEquals(inputOdd.name, result[0].name) // Ensure other properties are preserved
        assertEquals(inputOdd.id, result[0].id)
    }

    @Test
    fun `processOddsUpdate - standard odd - oddsValue does not go below MIN_ODDS_VALUE_THRESHOLD`() {
        val inputOdd = createOdd(name = "Standard Bet Low Odds", sellIn = 10, oddsValue = 0) // Already at min
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(0, result[0].oddsValue)
        assertEquals(9, result[0].sellIn)
    }

    @Test
    fun `processOddsUpdate - standard odd - when sellIn passed - oddsValue decreases twice as fast`() {
        val inputOdd = createOdd(name = "Standard Bet SellIn Passed", sellIn = -1, oddsValue = 20)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(-2, result[0].sellIn)
        assertEquals(18, result[0].oddsValue) // 20 -> 19 (normal) -> 18 (sellIn passed)
    }

    @Test
    fun `processOddsUpdate - standard odd - when sellIn passed and odds low - oddsValue does not go below MIN_ODDS_VALUE_THRESHOLD`() {
        val inputOdd = createOdd(name = "Standard Bet SellIn Passed Low Odds", sellIn = -1, oddsValue = 1)
        val result = processor.processOddsUpdate(listOf(inputOdd))
        assertEquals(0, result[0].oddsValue) // 1 -> 0 (normal) -> still 0 (sellIn passed, hits min)
    }

    // --- "First Goal Scorer" Odds Tests ---

    @Test
    fun `processOddsUpdate - firstGoalScorer - sellIn does not decrease, oddsValue decreases by 1`() {
        val inputOdd = createOdd(name = OddType.FirstGoalScorer.displayName, sellIn = 10, oddsValue = 30)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(10, result[0].sellIn) // sellIn unchanged
        assertEquals(29, result[0].oddsValue)
    }

    @Test
    fun `processOddsUpdate - firstGoalScorer - oddsValue does not go below MIN_ODDS_VALUE_THRESHOLD`() {
        val inputOdd = createOdd(name = OddType.FirstGoalScorer.displayName, sellIn = 10, oddsValue = 0)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(0, result[0].oddsValue)
        assertEquals(10, result[0].sellIn)
    }

    @Test
    fun `processOddsUpdate - firstGoalScorer - when sellIn passed - oddsValue still decreases by 1 (sellIn still no change)`() {
        val inputOdd = createOdd(name = OddType.FirstGoalScorer.displayName, sellIn = -1, oddsValue = 30)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(-1, result[0].sellIn) // Original sellIn passed in, not decreased by processor's main logic
        assertEquals(29, result[0].oddsValue) // Decreased once by the initial rule
    }


    // --- "Total Score" Odds Tests ---

    @Test
    fun `processOddsUpdate - totalScore - sellIn decreases by 1, oddsValue increases by 1`() {
        val inputOdd = createOdd(name = OddType.TotalScore.displayName, sellIn = 10, oddsValue = 20)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(9, result[0].sellIn)
        assertEquals(21, result[0].oddsValue)
    }

    @Test
    fun `processOddsUpdate - totalScore - oddsValue does not exceed MAX_ODDS_VALUE`() {
        val inputOdd = createOdd(name = OddType.TotalScore.displayName, sellIn = 10, oddsValue = 50) // Already at max
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(50, result[0].oddsValue)
        assertEquals(9, result[0].sellIn)
    }

    @Test
    fun `processOddsUpdate - totalScore - when sellIn passed - oddsValue increases again`() {
        val inputOdd = createOdd(name = OddType.TotalScore.displayName, sellIn = -1, oddsValue = 20)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(-2, result[0].sellIn)
        assertEquals(22, result[0].oddsValue) // 20 -> 21 (normal) -> 22 (sellIn passed)
    }

    @Test
    fun `processOddsUpdate - totalScore - when sellIn passed and odds near max - caps at MAX_ODDS_VALUE`() {
        val inputOdd = createOdd(name = OddType.TotalScore.displayName, sellIn = -1, oddsValue = 49)
        val result = processor.processOddsUpdate(listOf(inputOdd))
        assertEquals(50, result[0].oddsValue) // 49 -> 50 (normal) -> still 50 (sellIn passed, hits max)
    }

    // --- "Number of Fouls" Odds Tests (ASSUMES BUG FIX in OddsLogicProcessor) ---

    @Test
    fun `processOddsUpdate - numberOfFouls - sellIn decreases, oddsValue increases normally when sellIn high`() {
        val inputOdd = createOdd(name = OddType.NumberOfFouls.displayName, sellIn = 15, oddsValue = 20) // sellIn > 11
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(14, result[0].sellIn)
        assertEquals(21, result[0].oddsValue) // Base increase
    }

    @Test
    fun `processOddsUpdate - numberOfFouls - sellIn below primary threshold - odds increase bonus`() {
        // sellIn = 10 (which is < FOULS_SELL_IN_THRESHOLD_PRIMARY = 11)
        // Expected: +1 (base) +1 (bonus for <11) = +2 total increase for odds
        val inputOdd = createOdd(name = OddType.NumberOfFouls.displayName, sellIn = 10, oddsValue = 20)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(9, result[0].sellIn)
        assertEquals(22, result[0].oddsValue)
    }

    @Test
    fun `processOddsUpdate - numberOfFouls - sellIn below secondary threshold - odds increase double bonus`() {
        // sellIn = 5 (which is < FOULS_SELL_IN_THRESHOLD_SECONDARY = 6, and also < 11)
        // Expected: +1 (base) +1 (bonus for <11) +1 (bonus for <6) = +3 total increase for odds
        val inputOdd = createOdd(name = OddType.NumberOfFouls.displayName, sellIn = 5, oddsValue = 20)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(4, result[0].sellIn)
        assertEquals(23, result[0].oddsValue)
    }

    @Test
    fun `processOddsUpdate - numberOfFouls - bonus increases do not exceed MAX_ODDS_VALUE`() {
        val inputOdd = createOdd(name = OddType.NumberOfFouls.displayName, sellIn = 5, oddsValue = 48)
        // Expected: 48 -> 49 (base) -> 50 (bonus <11) -> still 50 (bonus <6, hits max)
        val result = processor.processOddsUpdate(listOf(inputOdd))
        assertEquals(50, result[0].oddsValue)
    }

    @Test
    fun `processOddsUpdate - numberOfFouls - bonus increases when starting near MAX_ODDS_VALUE`() {
        // Test case: sellIn is low enough for bonus, but initial odds is high, but not maxed.
        // e.g., sellIn = 5 (eligible for +3 total), odds = 47
        // 47 -> 48 (base) -> 49 (bonus for <11) -> 50 (bonus for <6) -> capped at 50
        val inputOdd = createOdd(name = OddType.NumberOfFouls.displayName, sellIn = 5, oddsValue = 47)
        val result = processor.processOddsUpdate(listOf(inputOdd))
        assertEquals(50, result[0].oddsValue)
    }


    @Test
    fun `processOddsUpdate - numberOfFouls - when sellIn passed - oddsValue becomes 0`() {
        val inputOdd = createOdd(name = OddType.NumberOfFouls.displayName, sellIn = -1, oddsValue = 20)
        val result = processor.processOddsUpdate(listOf(inputOdd))

        assertEquals(-2, result[0].sellIn) // sellIn still decreases
        assertEquals(0, result[0].oddsValue)
    }

    // --- Multiple Items in List Test ---

    @Test
    fun `processOddsUpdate - updates multiple items correctly in a list`() {
        val odd1_standard = createOdd(name = "Standard Bet", sellIn = 10, oddsValue = 20)
        val odd2_totalScore = createOdd(name = OddType.TotalScore.displayName, sellIn = 5, oddsValue = 30)
        val odd3_firstGoal = createOdd(name = OddType.FirstGoalScorer.displayName, sellIn = 12, oddsValue = 15)
        val odd4_fouls = createOdd(name = OddType.NumberOfFouls.displayName, sellIn = 3, oddsValue = 25) // below secondary threshold
        val inputList = listOf(odd1_standard, odd2_totalScore, odd3_firstGoal, odd4_fouls)

        val result = processor.processOddsUpdate(inputList)
        assertEquals(4, result.size)

        // Check odd1 (Standard Bet: sellIn 10->9, odds 20->19)
        val resultOdd1 = result.find { it.id == odd1_standard.id }!!
        assertEquals(9, resultOdd1.sellIn)
        assertEquals(19, resultOdd1.oddsValue)

        // Check odd2 (Total Score: sellIn 5->4, odds 30->31)
        val resultOdd2 = result.find { it.id == odd2_totalScore.id }!!
        assertEquals(4, resultOdd2.sellIn)
        assertEquals(31, resultOdd2.oddsValue)

        // Check odd3 (First Goal Scorer: sellIn 12->12, odds 15->14)
        val resultOdd3 = result.find { it.id == odd3_firstGoal.id }!!
        assertEquals(12, resultOdd3.sellIn)
        assertEquals(14, resultOdd3.oddsValue)

        // Check odd4 (Number of Fouls: sellIn 3->2, odds 25 -> 26(base) -> 27(<11) -> 28(<6))
        val resultOdd4 = result.find { it.id == odd4_fouls.id }!!
        assertEquals(2, resultOdd4.sellIn)
        assertEquals(28, resultOdd4.oddsValue)
    }

    @Test
    fun `processOddsUpdate - empty input list - returns empty list`() {
        val inputList = emptyList<Odd>()
        val result = processor.processOddsUpdate(inputList)
        assertEquals(0, result.size)
    }
}