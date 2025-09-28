package com.betsson.interviewtest.data.logic.strategies

import com.betsson.interviewtest.data.logic.LogicConstants
import com.betsson.interviewtest.data.logic.OddUpdateStrategy
import com.betsson.interviewtest.domain.model.Odd

class TotalScoreStrategy : OddUpdateStrategy {
    override fun update(odd: Odd): Odd {
        var newSellIn = odd.sellIn
        var newOddsValue = odd.oddsValue

        // Total score increases in quality over time
        if (newOddsValue < LogicConstants.MAX_ODDS_VALUE) {
            newOddsValue += LogicConstants.SPECIAL_ODDS_INCREASE
        }

        // Decrease sellIn
        newSellIn -= LogicConstants.STANDARD_SELL_IN_DECREASE

        // After sell date, quality increases even more
        if (newSellIn < LogicConstants.SELL_IN_PASSED_THRESHOLD) {
            if (newOddsValue < LogicConstants.MAX_ODDS_VALUE) {
                newOddsValue += LogicConstants.TOTAL_SCORE_ODDS_INCREASE_WHEN_SELL_IN_PASSED
            }
        }

        return odd.copy(sellIn = newSellIn, oddsValue = newOddsValue)
    }
}