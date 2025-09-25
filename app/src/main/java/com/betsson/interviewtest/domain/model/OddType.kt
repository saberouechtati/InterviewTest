package com.betsson.interviewtest.domain.model

private const val TOTAL_SCORE = "Total score"
private const val NUMBER_OF_FOULS = "Number of fouls"
private const val FIRST_GOAL_SCORER = "First goal scorer"

sealed class OddType(val displayName: String) {
    object TotalScore : OddType(TOTAL_SCORE)
    object NumberOfFouls : OddType(NUMBER_OF_FOULS)
    object FirstGoalScorer : OddType(FIRST_GOAL_SCORER)
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