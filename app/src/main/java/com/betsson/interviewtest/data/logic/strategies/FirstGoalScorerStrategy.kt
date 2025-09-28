package com.betsson.interviewtest.data.logic.strategies

import com.betsson.interviewtest.data.logic.OddUpdateStrategy
import com.betsson.interviewtest.domain.model.Odd

class FirstGoalScorerStrategy : OddUpdateStrategy {
    override fun update(odd: Odd): Odd {
        // First goal scorer never changes - return as is
        return odd
    }
}