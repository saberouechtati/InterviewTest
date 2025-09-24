package com.betsson.interviewtest.data.test

import com.betsson.interviewtest.data.dto.Bet
import com.betsson.interviewtest.domain.model.OddType

// Test data helpers
object TestData {
    fun createTestOdds() = Bet(OddType.fromName("Winning team"), 10, 20, "https://i.imgur.com/mx66SBD.jpeg").mapBetToOdd()

    fun createTestOddsList() = listOf(
        Bet(OddType.fromName("Winning team"), 10, 20, "https://i.imgur.com/mx66SBD.jpeg").mapBetToOdd(),
        Bet(OddType.fromName("Total score"), 2, 0, "https://i.imgur.com/VnPRqcv.jpeg").mapBetToOdd(),
        Bet(OddType.fromName("Player performance"), 5, 7, "https://i.imgur.com/Urpc00H.jpeg").mapBetToOdd(),
        Bet(OddType.fromName("First goal scorer"), 0, 80, "https://i.imgur.com/Wy94Tt7.jpeg").mapBetToOdd(),
        Bet(OddType.fromName("Number of fouls"), 5, 49, "https://i.imgur.com/NMLpcKj.jpeg").mapBetToOdd(),
        Bet(OddType.fromName("Corner kicks"), 3, 6, "https://i.imgur.com/TiJ8y5l.jpeg").mapBetToOdd()
    )

    fun createTestInitialOddsList() = listOf(
        Bet(OddType.fromName("Initial"), 5, 0, "https://i.imgur.com/mx66SBD.jpeg").mapBetToOdd()
    )

    fun createTestUpdatedOddsList() = listOf(
        Bet(OddType.fromName("Updated"), 10, 20, "https://i.imgur.com/mx66SBD.jpeg").mapBetToOdd()
    )

    fun createTestProblematicOddsList() = listOf(Bet(OddType.fromName("Problematic"), 10, 20, "").mapBetToOdd())
}