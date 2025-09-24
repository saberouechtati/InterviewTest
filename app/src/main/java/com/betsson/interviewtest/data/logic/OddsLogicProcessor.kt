package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType

class OddsLogicProcessor {

    companion object {
        const val MAX_ODDS_VALUE = 50
        const val MIN_ODDS_VALUE_THRESHOLD = 0 // For checks like `newOddsValue > 0`

        // General adjustments
        const val STANDARD_ODDS_DECREASE = 1
        const val STANDARD_SELL_IN_DECREASE = 1
        const val SPECIAL_ODDS_INCREASE = 1 // For "Total score", "Number of fouls"

        // "Number of fouls" specific thresholds and adjustments
        const val FOULS_SELL_IN_THRESHOLD_PRIMARY = 11 // Original: newSellIn < 11
        const val FOULS_SELL_IN_THRESHOLD_SECONDARY = 6  // Original: newSellIn < 6
        const val FOULS_ODDS_BONUS_INCREASE = 1      // The +1 adjustments for fouls

        // SellIn passed threshold
        const val SELL_IN_PASSED_THRESHOLD = 0 // For checks like `newSellIn < 0`

        // "Total score" adjustments when sellIn passed
        const val TOTAL_SCORE_ODDS_INCREASE_WHEN_SELL_IN_PASSED = 1 // The +1 for "Total score" if newSellIn < 0

    }

    // This function directly adapts the logic from MainActivity.calculateOdds()
    // to work with a list of our domain.model.Odd objects.
    fun processOddsUpdate(oddsList: List<Odd>): List<Odd> {
        // Important: The original logic mutated the list in place.
        // For better practice in our new architecture, we should return a new list
        // with updated items, or if mutating, ensure the input list is a mutable copy.
        // Here, we'll create new Odd instances for the updated values.

        return oddsList.map { currentOdd ->
            // Create a mutable copy to work with, as Odd's sellIn and oddsValue are var
            var newSellIn = currentOdd.sellIn
            var newOddsValue = currentOdd.oddsValue
            val oddType = OddType.fromName(currentOdd.name) // 'name' in Odd corresponds to 'oddType' in old Bet

            if (oddType !is OddType.TotalScore && oddType !is OddType.NumberOfFouls) {
                if (newOddsValue > MIN_ODDS_VALUE_THRESHOLD) {
                    if (oddType !is OddType.FirstGoalScorer) {
                        newOddsValue -= STANDARD_ODDS_DECREASE
                    }
                }
            } else {
                if (newOddsValue < MAX_ODDS_VALUE) {
                    newOddsValue += SPECIAL_ODDS_INCREASE
                    if (oddType is OddType.NumberOfFouls) {
                        if (newSellIn < FOULS_SELL_IN_THRESHOLD_PRIMARY) {
                            if (newOddsValue < MAX_ODDS_VALUE) {
                                newOddsValue += FOULS_ODDS_BONUS_INCREASE
                            }
                        }
                        if (newSellIn < FOULS_SELL_IN_THRESHOLD_SECONDARY) {
                            if (newOddsValue < MAX_ODDS_VALUE) {
                                newOddsValue += FOULS_ODDS_BONUS_INCREASE
                            }
                        }
                    }
                }
            }

            if (oddType !is OddType.FirstGoalScorer) {
                newSellIn -= STANDARD_SELL_IN_DECREASE
            }

            if (newSellIn < SELL_IN_PASSED_THRESHOLD) {
                if (oddType !is OddType.TotalScore) {
                    if (oddType !is OddType.NumberOfFouls) {
                        if (newOddsValue > MIN_ODDS_VALUE_THRESHOLD) {
                            if (oddType !is OddType.FirstGoalScorer) {
                                newOddsValue -= STANDARD_ODDS_DECREASE
                            }
                        }
                    } else {
                        // This effectively sets odds to 0
                        newOddsValue -= newOddsValue
                    }
                } else { // "Total score"
                    if (newOddsValue < MAX_ODDS_VALUE) {
                        newOddsValue += TOTAL_SCORE_ODDS_INCREASE_WHEN_SELL_IN_PASSED
                    }
                }
            }

            currentOdd.copy(sellIn = newSellIn, oddsValue = newOddsValue)
        }
    }
}
