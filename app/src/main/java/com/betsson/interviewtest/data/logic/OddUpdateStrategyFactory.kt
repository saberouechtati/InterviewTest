package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.data.logic.strategies.FirstGoalScorerStrategy
import com.betsson.interviewtest.data.logic.strategies.NumberOfFoulsStrategy
import com.betsson.interviewtest.data.logic.strategies.RegularBetStrategy
import com.betsson.interviewtest.data.logic.strategies.TotalScoreStrategy
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType
import jakarta.inject.Inject

class OddUpdateStrategyFactory @Inject constructor() {
    fun createStrategy(odd: Odd): OddUpdateStrategy {
        val oddType = OddType.fromName(odd.name)
        return when (oddType) {
            is OddType.FirstGoalScorer -> FirstGoalScorerStrategy()
            is OddType.TotalScore -> TotalScoreStrategy()
            is OddType.NumberOfFouls -> NumberOfFoulsStrategy()
            else -> RegularBetStrategy()
        }
    }
}