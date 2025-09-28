package com.betsson.interviewtest.data.logic.strategies

import com.betsson.interviewtest.data.logic.LogicConstants
import com.betsson.interviewtest.data.logic.OddUpdateStrategy
import com.betsson.interviewtest.domain.model.Odd

class NumberOfFoulsStrategy : OddUpdateStrategy {
    override fun update(odd: Odd): Odd {
        var newSellIn = odd.sellIn
        var newOddsValue = odd.oddsValue

        // Increase quality as it approaches
        if (newOddsValue < LogicConstants.MAX_ODDS_VALUE) {
            newOddsValue += LogicConstants.SPECIAL_ODDS_INCREASE

            // Additional increases based on sellIn
            if (newSellIn < LogicConstants.FOULS_SELL_IN_THRESHOLD_PRIMARY &&
                newOddsValue < LogicConstants.MAX_ODDS_VALUE) {
                newOddsValue += LogicConstants.FOULS_ODDS_BONUS_INCREASE
            }
            if (newSellIn < LogicConstants.FOULS_SELL_IN_THRESHOLD_SECONDARY &&
                newOddsValue < LogicConstants.MAX_ODDS_VALUE) {
                newOddsValue += LogicConstants.FOULS_ODDS_BONUS_INCREASE
            }
        }

        // Decrease sellIn
        newSellIn -= LogicConstants.STANDARD_SELL_IN_DECREASE

        // Handle expired - quality drops to 0
        if (newSellIn < LogicConstants.SELL_IN_PASSED_THRESHOLD) {
            newOddsValue = 0
        }

        return odd.copy(sellIn = newSellIn, oddsValue = newOddsValue)
    }
}