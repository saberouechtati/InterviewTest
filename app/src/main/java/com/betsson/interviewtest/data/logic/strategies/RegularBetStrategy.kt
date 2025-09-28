package com.betsson.interviewtest.data.logic.strategies

import com.betsson.interviewtest.data.logic.LogicConstants
import com.betsson.interviewtest.data.logic.OddUpdateStrategy
import com.betsson.interviewtest.domain.model.Odd

class RegularBetStrategy : OddUpdateStrategy {
    override fun update(odd: Odd): Odd {
        var newSellIn = odd.sellIn
        var newOddsValue = odd.oddsValue

        // Regular bets decrease in quality
        if (newOddsValue > LogicConstants.MIN_ODDS_VALUE_THRESHOLD) {
            newOddsValue -= LogicConstants.STANDARD_ODDS_DECREASE
        }

        // Decrease sellIn
        newSellIn -= LogicConstants.STANDARD_SELL_IN_DECREASE

        // After sell date, quality degrades twice as fast
        if (newSellIn < LogicConstants.SELL_IN_PASSED_THRESHOLD) {
            if (newOddsValue > LogicConstants.MIN_ODDS_VALUE_THRESHOLD) {
                newOddsValue -= LogicConstants.STANDARD_ODDS_DECREASE
            }
        }

        return odd.copy(sellIn = newSellIn, oddsValue = newOddsValue)
    }
}