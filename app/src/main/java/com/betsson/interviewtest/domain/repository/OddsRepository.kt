package com.betsson.interviewtest.domain.repository

import com.betsson.interviewtest.domain.model.Odd
import kotlinx.coroutines.flow.Flow

interface OddsRepository {
    fun getOddsStream(): Flow<List<Odd>>
    suspend fun triggerOddsUpdate()
}