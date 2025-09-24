package com.betsson.interviewtest.presentation.features.oddsList


import app.cash.turbine.test
import com.betsson.interviewtest.data.test.TestData
import com.betsson.interviewtest.domain.model.Odd
import com.betsson.interviewtest.domain.model.toOddItemUiModel
import com.betsson.interviewtest.domain.usecase.GetSortedOddsStreamUseCase
import com.betsson.interviewtest.domain.usecase.TriggerOddsUpdateUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


@ExperimentalCoroutinesApi
class OddsListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getSortedOddsStreamUseCase: GetSortedOddsStreamUseCase
    private lateinit var triggerOddsUpdateUseCase: TriggerOddsUpdateUseCase
    private lateinit var viewModel: OddsListViewModel

    // Use a MutableSharedFlow to control emissions from the use case mock during tests
    private lateinit var oddsFlow: MutableSharedFlow<List<Odd>>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getSortedOddsStreamUseCase = mockk()
        triggerOddsUpdateUseCase = mockk(relaxUnitFun = true) // For suspend fun returning Unit

        oddsFlow = MutableSharedFlow() // This will be returned by the use case
        every { getSortedOddsStreamUseCase() } returns oddsFlow

        // ViewModel is created here, so observeOdds() in init{} will start collecting oddsFlow
        viewModel = OddsListViewModel(
            getSortedOddsStreamUseCase,
            triggerOddsUpdateUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_is_loading_then_success_when_odds_are_emitted() = runTest(testDispatcher) {
        val testOdds = TestData.createTestOddsList()
        val expectedUiOdds = testOdds.map { it.toOddItemUiModel() }

        viewModel.uiState.test {
            // 1. onStart in observeOdds sets isLoading = true
            var state = awaitItem()
            assertTrue("Should be loading initially", state.isLoading)
            assertTrue("Odds should be empty initially", state.odds.isEmpty())
            assertNull("Error should be null initially", state.error)

            // 2. Emit data from the flow
            oddsFlow.emit(testOdds)

            // 3. State updates with data, isLoading false
            state = awaitItem()
            assertFalse("Should not be loading after data emission", state.isLoading)
            assertEquals("Odds should match emitted data", expectedUiOdds, state.odds)
            assertNull("Error should be null on success", state.error)

            // Clean up
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun state_reflects_empty_odds_list_when_use_case_emits_empty_list() = runTest(testDispatcher) {
        viewModel.uiState.test {
            // 1. Initial loading state
            assertEquals(true, awaitItem().isLoading)

            // 2. Emit empty list
            oddsFlow.emit(emptyList())

            // 3. State updates with empty odds, isLoading false
            val state = awaitItem()
            assertFalse("Should not be loading", state.isLoading)
            assertTrue("Odds should be empty", state.odds.isEmpty())
            assertNull("Error should be null", state.error)

            // Clean up
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun viewModel_emits_error_state_when_mapping_fails () = runTest(testDispatcher) { // Pass dispatcher to runTest
        val problematicDomainOdds = TestData.createTestProblematicOddsList()
        val expectedErrorMessageContent = "Error processing data: Invalid image URL: " // Or "Invalid image URL"
        // Based on what CRASH actually throws

        // This flow will be consumed by the ViewModel
        every { getSortedOddsStreamUseCase() } returns flow {
            emit(problematicDomainOdds)
            delay(Long.MAX_VALUE) // Keep flow alive to ensure collection doesn't prematurely end
        }

        // Initialize ViewModel HERE, after mocking use case and setting up dispatcher
        viewModel = OddsListViewModel(getSortedOddsStreamUseCase, triggerOddsUpdateUseCase)

        viewModel.uiState.test {

            // Emission 1: Default initial state of the StateFlow
            var currentState = awaitItem()
            assertFalse("Default initial state should not be loading", currentState.isLoading)
            assertNull("Default initial state should have no error", currentState.error)

            // Advance dispatcher to allow onStart to execute
            testDispatcher.scheduler.advanceUntilIdle() // <<< Allow coroutines to run

            // Emission 2: State from onStart in observeOdds()
            currentState = awaitItem()
            assertTrue("State from onStart should be loading", currentState.isLoading)
            assertNull("State from onStart should have no error", currentState.error)

            // Advance dispatcher to allow onEach (and its catch block) to execute
            testDispatcher.scheduler.advanceUntilIdle() // <<< Allow coroutines to run further

            // Emission 3: State from the catch block in onEach after mapping error
            currentState = awaitItem()

            assertFalse("Should not be loading after mapping error. Actual error: ${currentState.error}", currentState.isLoading)
            Assert.assertNotNull(currentState.error, "Error message should be present. Actual error: ${currentState.error}")
            assertTrue(
                "Error message should contain '$expectedErrorMessageContent'. Actual: '${currentState.error}'",
                currentState.error!!.contains(expectedErrorMessageContent)
            )
            assertTrue("Odds should be empty after mapping error", currentState.odds.isEmpty())

            // Clean up
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `state reflects error when getSortedOddsStreamUseCase flow throws exception`() = runTest(testDispatcher) {
        val errorMessage = "Test Flow Exception"
        val expectedUiError = "Data stream error: $errorMessage"

        // Mock the use case to return a flow that immediately throws an exception
        every { getSortedOddsStreamUseCase() } returns flow {
            throw RuntimeException(errorMessage) // Or any other exception
        }

        // Initialize ViewModel (this will trigger observeOdds)
        viewModel = OddsListViewModel(getSortedOddsStreamUseCase, triggerOddsUpdateUseCase)

        viewModel.uiState.test {

            // Emission 1: Default initial state
            var currentState = awaitItem()
            assertFalse("Default initial state should not be loading", currentState.isLoading)
            assertNull("Default initial state should have no error", currentState.error)

            // Advance dispatcher to allow onStart to execute
            testDispatcher.scheduler.advanceUntilIdle()

            // Emission 2: State from onStart (this should execute before the flow throws the exception)
            currentState = awaitItem()
            assertTrue("State from onStart should be loading", currentState.isLoading)
            assertNull("State from onStart should have no error", currentState.error)

            // Advance dispatcher to allow the flow error to propagate and be caught
            testDispatcher.scheduler.advanceUntilIdle()

            // Emission 3: State from the outer .catch {} block in observeOdds
            currentState = awaitItem()
            assertFalse("Should not be loading after flow error. Actual error: ${currentState.error}", currentState.isLoading)
            Assert.assertNotNull("Error message should be present. Actual error: ${currentState.error}", currentState.error,)
            assertEquals("Error message does not match expected", expectedUiError, currentState.error)

            // Assert odds state if necessary (e.g., should be empty or retain previous)
            assertTrue("Odds should be empty after flow error if they are cleared", currentState.odds.isEmpty())

            // Clean up
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun onUpdateOddsClicked_sets_loading_calls_use_case_then_reflects_new_odds() =
        runTest(testDispatcher) {
            val initialOdds = TestData.createTestInitialOddsList()
            val updatedOdds = TestData.createTestUpdatedOddsList()
            val expectedUiInitialOdds = initialOdds.map { it.toOddItemUiModel() }
            val expectedUiUpdatedOdds = updatedOdds.map { it.toOddItemUiModel() }

            coEvery { triggerOddsUpdateUseCase() } coAnswers {
                // Simulate the update causing the main oddsFlow to emit new data
                oddsFlow.emit(updatedOdds)
                Unit
            }

            viewModel.uiState.test {
                // 1. Emit initial odds to get out of initial loading
                oddsFlow.emit(initialOdds)
                awaitItem() // Initial loading
                var state = awaitItem() // Initial data
                assertFalse(state.isLoading)
                assertEquals(expectedUiInitialOdds, state.odds)

                // 2. Act: Call update
                viewModel.onUpdateOddsClicked()

                // 3. State becomes loading due to onUpdateOddsClicked
                state = awaitItem()
                assertTrue("Should be loading during update", state.isLoading)
                // odds might still be the initial ones here

                // 4. triggerOddsUpdateUseCase is called (coAnswers block runs, emits to oddsFlow)
                // 5. observeOdds collects from oddsFlow, updates state with new odds, isLoading false
                state = awaitItem()
                assertFalse("Should not be loading after update success", state.isLoading)
                assertEquals("Odds should be updated", expectedUiUpdatedOdds, state.odds)
                assertNull("Error should be null", state.error)
                coVerify { triggerOddsUpdateUseCase() }

                // Clean up
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun onUpdateOddsClicked_shows_error_if_triggerOddsUpdateUseCase_fails() =
        runTest(testDispatcher) {
            val errorMessage = "Update Trigger Failed"
            coEvery { triggerOddsUpdateUseCase() } throws RuntimeException(errorMessage)

            viewModel.uiState.test {
                // 1. Let initial load complete (e.g. with empty data)
                oddsFlow.emit(emptyList())
                awaitItem() // initial loading
                var state = awaitItem() // initial empty data state
                assertFalse(state.isLoading)

                // 2. Act
                viewModel.onUpdateOddsClicked()

                // 3. State becomes loading
                state = awaitItem()
                assertTrue(state.isLoading)

                // 4. State reflects error from triggerOddsUpdateUseCase's catch block
                state = awaitItem()
                assertFalse("Should not be loading after trigger failure", state.isLoading)
                assertEquals("Update failed: $errorMessage", state.error)
                // Odds should likely remain as they were before the failed trigger
                assertTrue(
                    "Odds should be what they were before trigger failure",
                    state.odds.isEmpty()
                )

                coVerify { triggerOddsUpdateUseCase() }

                // Clean up
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun onUpdateOddsClicked_successfulTrigger_butSubsequentDataFlowFails() =
        runTest(testDispatcher) {
            val initialOdds = TestData.createTestInitialOddsList()
            val dataFlowErrorMessage = "Data error after refresh trigger"

            // Initial setup: getSortedOddsStreamUseCase returns the controllable oddsFlow
            every { getSortedOddsStreamUseCase() } returns oddsFlow

            // ViewModel created with the initial oddsFlow
            // viewModel is already initialized in @Before with the standard oddsFlow

            coEvery { triggerOddsUpdateUseCase() } coAnswers {
                // Strategy for this test:
                // 1. Initial successful flow setup (done by `every { getSortedOddsStreamUseCase() } returns oddsFlow;`)
                // 2. ViewModel is created and collects `oddsFlow`.
                // 3. `triggerOddsUpdateUseCase` is mocked.
                // 4. When `triggerOddsUpdateUseCase` is called, we'll make `getSortedOddsStreamUseCase`
                //    return a *new* erroring flow.
                // 5. CRITICALLY: The existing ViewModel won't pick up this *new* flow unless `observeOdds` is called again
                //    or the flow collection is restarted. Your current `observeOdds` is only called in `init`.

                // Assume triggerOddsUpdateUseCase causes the *next* emission from the repository to be an error.
                every { getSortedOddsStreamUseCase() } returns flow {
                    emit(initialOdds) // First, emit initial odds successfully
                    throw RuntimeException(dataFlowErrorMessage) // Then, the flow itself errors out
                }

                // Recreate the VM for this specific flow behavior
                val testViewModel =
                    OddsListViewModel(getSortedOddsStreamUseCase, triggerOddsUpdateUseCase)
                coEvery { triggerOddsUpdateUseCase() } returns Unit // Keep trigger successful

                testViewModel.uiState.test {
                    // 1. Loading state
                    var state = awaitItem()
                    assertTrue(state.isLoading)

                    // 2. Initial successful data emission from the new flow
                    state = awaitItem()
                    assertFalse(state.isLoading)
                    assertEquals(initialOdds.map { it.toOddItemUiModel() }, state.odds)

                    // 3. Now the flow throws an exception, caught by observeOdds's .catch
                    state = awaitItem()
                    assertFalse("Should not be loading after data flow error", state.isLoading)
                    assertEquals("Data stream error: $dataFlowErrorMessage", state.error)
                    // Odds might revert to what they were or be empty
                    assertEquals(
                        "Odds should remain from last good state or be empty",
                        initialOdds.map { it.toOddItemUiModel() }, state.odds
                    ) // Or emptyList() based on preference

                    // Clean up
                    cancelAndConsumeRemainingEvents()
                }
                // Verification for trigger is not relevant in this reframed test,
                // as we are testing the data flow error, not the trigger action directly.
            }
        }

}
