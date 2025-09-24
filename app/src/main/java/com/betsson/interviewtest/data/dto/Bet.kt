package com.betsson.interviewtest.data.dto

import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.OddType
import java.util.Locale
import java.util.UUID

data class Bet(var type: OddType, var sellIn: Int, var odds: Int, var image: String) {

    fun mapBetToOdd(): Odd {
        return Odd(
            id = this.type.displayName .replace(" ", "_").lowercase(Locale.getDefault()) + "_${UUID.randomUUID()}",
            name = this.type.displayName,
            sellIn = this.sellIn,
            oddsValue = this.odds,
            imageUrl = this.image
        )
    }
}