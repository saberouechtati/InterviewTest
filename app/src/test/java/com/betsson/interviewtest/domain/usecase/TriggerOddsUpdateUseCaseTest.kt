package com.betsson.interviewtest.domain.usecase

import com.betsson.interviewtest.domain.repository.OddsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.times

@ExperimentalCoroutinesApi
class TriggerOddsUpdateUseCaseTest {

    private lateinit var mockOddsRepository: OddsRepository
    private lateinit var triggerOddsUpdateUseCase: TriggerOddsUpdateUseCase

    @Before
    fun setUp() {
        mockOddsRepository = mock()
        triggerOddsUpdateUseCase = TriggerOddsUpdateUseCase(mockOddsRepository)
    }

    @Test
    fun `invoke - calls triggerOddsUpdate on repository`() = runTest {
        // When
        triggerOddsUpdateUseCase() // Use direct invocation because of 'operator fun invoke'

        // Then
        // Verify that oddsRepository.triggerOddsUpdate() was called exactly once
        verify(mockOddsRepository, times(1)).triggerOddsUpdate()
    }
}
