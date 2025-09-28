package com.betsson.interviewtest.data.logic

import com.betsson.interviewtest.domain.model.Odd
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class OddsLogicProcessorTest {

    // Mocks
    private lateinit var mockStrategyFactory: OddUpdateStrategyFactory
    private lateinit var mockUpdateStrategy: OddUpdateStrategy

    // Class under test
    private lateinit var processor: OddsLogicProcessor

    @Before
    fun setUp() {
        mockStrategyFactory = mock()
        mockUpdateStrategy = mock() // A generic mock strategy
        processor = OddsLogicProcessor(mockStrategyFactory)

        // Default behavior for mocks
        whenever(mockStrategyFactory.createStrategy(any())).thenReturn(mockUpdateStrategy)
        whenever(mockUpdateStrategy.update(any())).thenAnswer { invocation ->
            invocation.getArgument(0) as Odd // Default: return the same odd passed in
        }
    }

    private fun createOdd(name: String, sellIn: Int, oddsValue: Int, id: String = name): Odd {
        return Odd(id = id, name = name, sellIn = sellIn, oddsValue = oddsValue, imageUrl = "")
    }

    @Test
    fun `processOddsUpdate - calls strategy factory for each odd in the list`() {
        val odd1 = createOdd("Odd1", 10, 20)
        val odd2 = createOdd("Odd2", 5, 15)
        val inputList = listOf(odd1, odd2)
        processor.processOddsUpdate(inputList)

        verify(mockStrategyFactory, times(1)).createStrategy(odd1)
        verify(mockStrategyFactory, times(1)).createStrategy(odd2)
    }

    @Test
    fun `processOddsUpdate - calls update on strategy returned by factory for each odd`() {
        val odd1 = createOdd("Odd1", 10, 20)
        val odd2 = createOdd("Odd2", 5, 15)
        val inputList = listOf(odd1, odd2)

        // Specific mock strategies if needed to differentiate, but generic mockUpdateStrategy is often enough
        val mockStrategy1: OddUpdateStrategy = mock()
        val mockStrategy2: OddUpdateStrategy = mock()

        whenever(mockStrategyFactory.createStrategy(odd1)).thenReturn(mockStrategy1)
        whenever(mockStrategyFactory.createStrategy(odd2)).thenReturn(mockStrategy2)
        whenever(mockStrategy1.update(odd1)).thenReturn(odd1.copy(sellIn = 9)) // Simulate an update
        whenever(mockStrategy2.update(odd2)).thenReturn(odd2.copy(sellIn = 4)) // Simulate an update


        processor.processOddsUpdate(inputList)

        verify(mockStrategy1, times(1)).update(odd1)
        verify(mockStrategy2, times(1)).update(odd2)
    }

    @Test
    fun `processOddsUpdate - returns the list of odds updated by strategies`() {
        val odd1Input = createOdd("Odd1", 10, 20)
        val odd1Updated = odd1Input.copy(sellIn = 9, oddsValue = 19)

        val odd2Input = createOdd("Odd2", 5, 15)
        val odd2Updated = odd2Input.copy(sellIn = 4, oddsValue = 14)

        val inputList = listOf(odd1Input, odd2Input)

        // Setup mocks to return specific updated odds
        val mockStrategyOdd1: OddUpdateStrategy = mock()
        val mockStrategyOdd2: OddUpdateStrategy = mock()

        whenever(mockStrategyFactory.createStrategy(odd1Input)).thenReturn(mockStrategyOdd1)
        whenever(mockStrategyOdd1.update(odd1Input)).thenReturn(odd1Updated)

        whenever(mockStrategyFactory.createStrategy(odd2Input)).thenReturn(mockStrategyOdd2)
        whenever(mockStrategyOdd2.update(odd2Input)).thenReturn(odd2Updated)

        val result = processor.processOddsUpdate(inputList)

        assertEquals(2, result.size)
        assertEquals(odd1Updated, result.find { it.id == "Odd1" })
        assertEquals(odd2Updated, result.find { it.id == "Odd2" })
    }

    @Test
    fun `processOddsUpdate - empty input list - returns empty list and does not call factory`() {
        val inputList = emptyList<Odd>()
        val result = processor.processOddsUpdate(inputList)

        assertEquals(0, result.size)
        verify(mockStrategyFactory, times(0)).createStrategy(any())
        verify(mockUpdateStrategy, times(0)).update(any()) // Verifying the generic mock
    }
}
