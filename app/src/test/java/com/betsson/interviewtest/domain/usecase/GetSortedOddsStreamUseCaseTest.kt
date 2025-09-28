package com.betsson.interviewtest.domain.usecase

import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.repository.OddsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.times

@ExperimentalCoroutinesApi
class GetSortedOddsStreamUseCaseTest {

    private lateinit var mockOddsRepository: OddsRepository
    private lateinit var getSortedOddsStreamUseCase: GetSortedOddsStreamUseCase

    // Sample data for testing the flow emission
    private val sampleOdd1 = Odd("1", "Odd 1", 10, 20, "")
    private val sampleOdd2 = Odd("2", "Odd 2", 5, 15, "")
    private val sampleOddsList = listOf(sampleOdd1, sampleOdd2)
    private val sampleOddsFlow = flowOf(sampleOddsList)

    @Before
    fun setUp() {
        mockOddsRepository = mock()
        getSortedOddsStreamUseCase = GetSortedOddsStreamUseCase(mockOddsRepository)
    }

    @Test
    fun `invoke - calls getOddsStream on repository`() {
        // Given
        whenever(mockOddsRepository.getOddsStream()).thenReturn(sampleOddsFlow)

        // When
        getSortedOddsStreamUseCase() // Call invoke

        // Then
        // Verify that oddsRepository.getOddsStream() was called exactly once
        verify(mockOddsRepository, times(1)).getOddsStream()
    }

    @Test
    fun `invoke - returns the Flow provided by repository`() = runTest {
        // Given
        whenever(mockOddsRepository.getOddsStream()).thenReturn(sampleOddsFlow)

        // When
        val resultFlow = getSortedOddsStreamUseCase()

        // Then
        // Assert that the flow returned by the use case is the same as the one from the repository
        // And collect the first emission to ensure data integrity (optional but good)
        assertEquals(sampleOddsList, resultFlow.first())
        assertEquals(sampleOddsFlow, resultFlow) // Check if it's the exact same Flow instance
    }

    @Test
    fun `invoke - handles empty Flow from repository`() = runTest {
        // Given
        val emptyFlow = flowOf<List<Odd>>(emptyList())
        whenever(mockOddsRepository.getOddsStream()).thenReturn(emptyFlow)

        // When
        val resultFlow = getSortedOddsStreamUseCase()

        // Then
        assertEquals(emptyList<Odd>(), resultFlow.first())
        verify(mockOddsRepository, times(1)).getOddsStream()
    }
}
