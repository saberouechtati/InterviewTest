package com.betsson.interviewtest.domain.model

sealed class OddType(val displayName: String) {
    object TotalScore : OddType("Total score")
    object NumberOfFouls : OddType("Number of fouls")
    object FirstGoalScorer : OddType("First goal scorer")
    data class Other(val actualName: String) : OddType(actualName) // For any other types

    companion object {
        fun fromName(name: String): OddType = when (name) {
            TotalScore.displayName -> TotalScore
            NumberOfFouls.displayName -> NumberOfFouls
            FirstGoalScorer.displayName -> FirstGoalScorer
            else -> Other(name)
        }
    }
}