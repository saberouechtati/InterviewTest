package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.domain.model.Odd

interface OddUpdateStrategy {
    fun update(odd: Odd): Odd
}