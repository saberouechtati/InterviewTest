package com.betsson.interviewtest.data.logic

object LogicConstants {
    const val MAX_ODDS_VALUE = 50
    const val MIN_ODDS_VALUE_THRESHOLD = 0 // For checks like `newOddsValue > 0`

    // General adjustments
    const val STANDARD_ODDS_DECREASE = 1
    const val STANDARD_SELL_IN_DECREASE = 1
    const val SPECIAL_ODDS_INCREASE = 1 // For "Total score", "Number of fouls"

    // "Number of fouls" specific thresholds and adjustments
    const val FOULS_SELL_IN_THRESHOLD_PRIMARY = 11 // Original: newSellIn < 11
    const val FOULS_SELL_IN_THRESHOLD_SECONDARY = 6 // Original: newSellIn < 6
    const val FOULS_ODDS_BONUS_INCREASE = 1 // The +1 adjustments for fouls

    // SellIn passed threshold
    const val SELL_IN_PASSED_THRESHOLD = 0 // For checks like `newSellIn < 0`

    // "Total score" adjustments when sellIn passed
    const val TOTAL_SCORE_ODDS_INCREASE_WHEN_SELL_IN_PASSED =
        1 // The +1 for "Total score" if newSellIn < 0
}