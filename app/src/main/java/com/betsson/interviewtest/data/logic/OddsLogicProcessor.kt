package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.domain.model.Odd

class OddsLogicProcessor {

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
            val type = currentOdd.name // 'name' in Odd corresponds to 'type' in old Bet

            // -------- START OF COPIED/ADAPTED LOGIC from MainActivity.calculateOdds --------
            if (type != "Total score" && type != "Number of fouls") {
                if (newOddsValue > 0) {
                    if (type != "First goal scorer") {
                        newOddsValue -= 1
                    }
                }
            } else {
                if (newOddsValue < 50) {
                    newOddsValue += 1
                    if (type == "Number of fouls") {
                        if (newSellIn < 11) {
                            if (newOddsValue < 50) {
                                newOddsValue += 1
                            }
                        }
                        if (newSellIn < 6) {
                            if (newOddsValue < 50) {
                                newOddsValue += 1
                            }
                        }
                    }
                }
            }

            if (type != "First goal scorer") {
                newSellIn -= 1
            }

            if (newSellIn < 0) {
                if (type != "Total score") {
                    if (type != "Number of fouls") {
                        if (newOddsValue > 0) {
                            if (type != "First goal scorer") {
                                newOddsValue -= 1
                            }
                        }
                    } else {
                        // This effectively sets odds to 0
                        newOddsValue -= newOddsValue
                    }
                } else { // "Total score"
                    if (newOddsValue < 50) {
                        newOddsValue += 1
                    }
                }
            }
            // -------- END OF COPIED/ADAPTED LOGIC --------

            currentOdd.copy(sellIn = newSellIn, oddsValue = newOddsValue)
        }
    }
}
