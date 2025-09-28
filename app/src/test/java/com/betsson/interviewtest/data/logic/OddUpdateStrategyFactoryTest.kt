package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.data.logic.strategies.FirstGoalScorerStrategy
import com.betsson.interviewtest.data.logic.strategies.NumberOfFoulsStrategy
import com.betsson.interviewtest.data.logic.strategies.RegularBetStrategy
import com.betsson.interviewtest.data.logic.strategies.TotalScoreStrategy
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType
import org.junit.Assert.assertTrue
import org.junit.Test

class OddUpdateStrategyFactoryTest {

    private val factory = OddUpdateStrategyFactory()

    private fun createOdd(name: String, sellIn: Int = 0, oddsValue: Int = 0): Odd {
        return Odd(id = name, name = name, sellIn = sellIn, oddsValue = oddsValue, imageUrl = "")
    }

    @Test
    fun `createStrategy - returns FirstGoalScorerStrategy for FirstGoalScorer type`() {
        val odd = createOdd(name = OddType.FirstGoalScorer.displayName)
        val strategy = factory.createStrategy(odd)
        assertTrue(strategy is FirstGoalScorerStrategy)
    }

    @Test
    fun `createStrategy - returns TotalScoreStrategy for TotalScore type`() {
        val odd = createOdd(name = OddType.TotalScore.displayName)
        val strategy = factory.createStrategy(odd)
        assertTrue(strategy is TotalScoreStrategy)
    }

    @Test
    fun `createStrategy - returns NumberOfFoulsStrategy for NumberOfFouls type`() {
        val odd = createOdd(name = OddType.NumberOfFouls.displayName)
        val strategy = factory.createStrategy(odd)
        assertTrue(strategy is NumberOfFoulsStrategy)
    }

    @Test
    fun `createStrategy - returns RegularBetStrategy for unknown or standard bet names`() {
        val odd1 = createOdd(name = "Some Other Bet") // Unknown mapped to Standard/Regular
        val strategy1 = factory.createStrategy(odd1)
        assertTrue(strategy1 is RegularBetStrategy)

        // Assuming your OddType.fromName() maps "Standard Bet" to something that
        // defaults to RegularBetStrategy in the factory's when statement
        val odd2 = createOdd(name = "Standard Bet")
        val strategy2 = factory.createStrategy(odd2)
        assertTrue(strategy2 is RegularBetStrategy)
    }
}
