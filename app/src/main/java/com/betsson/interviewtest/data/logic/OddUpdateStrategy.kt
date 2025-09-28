package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.domain.model.Odd

fun interface OddUpdateStrategy {
    fun update(odd: Odd): Odd
}