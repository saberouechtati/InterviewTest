package com.betsson.interviewtest.presentation.features.oddsList


import app.cash.turbine.test
import com.betsson.interviewtest.data.test.DataSource
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

    companion object {
        private const val SHOULD_BE_LOADING_INITIALLY = "Should be loading initially"
        private const val ODDS_SHOULD_BE_EMPTY_INITIALLY = "Odds should be empty initially"
        private const val ERROR_SHOULD_BE_NULL_INITIALLY = "Error should be null initially"
        private const val SHOULD_NOT_BE_LOADING_AFTER_DATA_EMISSION =
            "Should not be loading after data emission"
        private const val ODDS_SHOULD_MATCH_EMITTED_DATA = "Odds should match emitted data"
        private const val ERROR_SHOULD_BE_NULL_ON_SUCCESS = "Error should be null on success"
        private const val SHOULD_NOT_BE_LOADING = "Should not be loading"
        private const val ERROR_PROCESSING_DATA_INVALID_IMAGE_URL =
            "Error processing data: Invalid image URL: "
        private const val ODDS_SHOULD_BE_EMPTY = "Odds should be empty"
        private const val ERROR_SHOULD_BE_NULL = "Error should be null"
        private const val DEFAULT_INITIAL_STATE_SHOULD_NOT_BE_LOADING =
            "Default initial state should not be loading"
        private const val ERROR = "error"
        private const val DEFAULT_INITIAL_STATE_SHOULD_HAVE_NO_ERROR =
            "Default initial state should have no $ERROR"
        private const val STATE_FROM_ON_START_SHOULD_BE_LOADING =
            "State from onStart should be loading"
        private const val STATE_FROM_ON_START_SHOULD_HAVE_NO_ERROR =
            "State from onStart should have no $ERROR"
        private const val ACTUAL = "Actual"
        private const val SHOULD_NOT_BE_LOADING_AFTER_MAPPING_ERROR_ACTUAL_ERROR =
            "Should not be loading after mapping $ERROR. $ACTUAL error"
        private const val ERROR_MESSAGE_SHOULD_CONTAIN = "Error message should contain"
        private const val TEST_FLOW_EXCEPTION = "Test Flow Exception"
        private const val ERROR_MESSAGE_SHOULD_BE_PRESENT_ACTUAL_ERROR =
            "Error message should be present. $ACTUAL $ERROR"
        private const val ODDS_SHOULD_BE_EMPTY_AFTER_MAPPING_ERROR =
            "Odds should be empty after mapping $ERROR"
        private const val DATA = "Data"
        private const val DATA_STREAM_ERROR = "$DATA stream $ERROR"
        private const val SHOULD_NOT_BE_LOADING_AFTER_FLOW_ERROR =
            "Should not be loading after flow $ERROR."
        private const val IF_THEY_ARE_CLEARED = "if they are cleared"
        private const val ODDS_SHOULD_REMAIN_FROM_LAST_GOOD_STATE_OR_BE_EMPTY =
            "Odds should remain from last good state or be empty"
        private const val SHOULD_NOT_BE_LOADING_AFTER_DATA_FLOW =
            "Should not be loading after data flow"
        private const val AFTER_REFRESH_TRIGGER = "after refresh trigger"
        private const val ODDS_SHOULD_BE_EMPTY_AFTER_FLOW = "Odds should be empty after flow"
        private const val UPDATE_FAILED = "Update failed"
        private const val ERROR_MESSAGE_DOES_NOT_MATCH_EXPECTED =
            "Error message does not match expected"
        private const val ODDS_SHOULD_BE_UPDATED = "Odds should be updated"
        private const val SHOULD_BE_LOADING_DURING_UPDATE = "Should be loading during update"
        private const val SHOULD_NOT_BE_LOADING_AFTER_UPDATE_SUCCESS =
            "Should not be loading after update success"
        private const val UPDATE_TRIGGER_FAILED = "Update Trigger Failed"
        private const val SHOULD_NOT_BE_LOADING_AFTER_TRIGGER_FAILURE =
            "Should not be loading after trigger failure"
        private const val ODDS_SHOULD_BE_WHAT_THEY_WERE_BEFORE_TRIGGER_FAILURE =
            "Odds should be what they were before trigger failure"
    }

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
    fun `initial state is loading then success when odds are emitted`() = runTest(testDispatcher) {
        val testOdds = DataSource.createTestOddsList()
        val expectedUiOdds = testOdds.map { it.toOddItemUiModel() }

        viewModel.uiState.test {
            // 1. onStart in observeOdds sets isLoading = true
            var state = awaitItem()
            assertTrue(SHOULD_BE_LOADING_INITIALLY, state.isLoading)
            assertTrue(ODDS_SHOULD_BE_EMPTY_INITIALLY, state.odds.isEmpty())
            assertNull(ERROR_SHOULD_BE_NULL_INITIALLY, state.error)

            // 2. Emit data from the flow
            oddsFlow.emit(testOdds)

            // 3. State updates with data, isLoading false
            state = awaitItem()
            assertFalse(SHOULD_NOT_BE_LOADING_AFTER_DATA_EMISSION, state.isLoading)
            assertEquals(ODDS_SHOULD_MATCH_EMITTED_DATA, expectedUiOdds, state.odds)
            assertNull(ERROR_SHOULD_BE_NULL_ON_SUCCESS, state.error)

            // Clean up
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `state reflects empty odds list when use case emits empty list`() =
        runTest(testDispatcher) {
            viewModel.uiState.test {
                // 1. Initial loading state
                assertEquals(true, awaitItem().isLoading)

                // 2. Emit empty list
                oddsFlow.emit(emptyList())

                // 3. State updates with empty odds, isLoading false
                val state = awaitItem()
                assertFalse(SHOULD_NOT_BE_LOADING, state.isLoading)
                assertTrue(ODDS_SHOULD_BE_EMPTY, state.odds.isEmpty())
                assertNull(ERROR_SHOULD_BE_NULL, state.error)

                // Clean up
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `viewModel emits error state when mapping fails`() =
        runTest(testDispatcher) { // Pass dispatcher to runTest
            val problematicDomainOdds = DataSource.createTestProblematicOddsList()
            val expectedErrorMessageContent =
                ERROR_PROCESSING_DATA_INVALID_IMAGE_URL // Or "Invalid image URL"
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
                assertFalse(DEFAULT_INITIAL_STATE_SHOULD_NOT_BE_LOADING, currentState.isLoading)
                assertNull(DEFAULT_INITIAL_STATE_SHOULD_HAVE_NO_ERROR, currentState.error)

                // Advance dispatcher to allow onStart to execute
                testDispatcher.scheduler.advanceUntilIdle() // <<< Allow coroutines to run

                // Emission 2: State from onStart in observeOdds()
                currentState = awaitItem()
                assertTrue(STATE_FROM_ON_START_SHOULD_BE_LOADING, currentState.isLoading)
                assertNull(STATE_FROM_ON_START_SHOULD_HAVE_NO_ERROR, currentState.error)

                // Advance dispatcher to allow onEach (and its catch block) to execute
                testDispatcher.scheduler.advanceUntilIdle() // <<< Allow coroutines to run further

                // Emission 3: State from the catch block in onEach after mapping error
                currentState = awaitItem()

                assertFalse(
                    "$SHOULD_NOT_BE_LOADING_AFTER_MAPPING_ERROR_ACTUAL_ERROR: ${currentState.error}",
                    currentState.isLoading
                )
                Assert.assertNotNull(
                    currentState.error,
                    "$ERROR_MESSAGE_SHOULD_BE_PRESENT_ACTUAL_ERROR: ${currentState.error}"
                )
                assertTrue(
                    "$ERROR_MESSAGE_SHOULD_CONTAIN '$expectedErrorMessageContent'. $ACTUAL: '${currentState.error}'",
                    currentState.error!!.contains(expectedErrorMessageContent)
                )
                assertTrue(ODDS_SHOULD_BE_EMPTY_AFTER_MAPPING_ERROR, currentState.odds.isEmpty())

                // Clean up
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `state reflects error when getSortedOddsStreamUseCase flow throws exception`() =
        runTest(testDispatcher) {
            val errorMessage = TEST_FLOW_EXCEPTION
            val expectedUiError = "$DATA_STREAM_ERROR: $errorMessage"

            // Mock the use case to return a flow that immediately throws an exception
            every { getSortedOddsStreamUseCase() } returns flow {
                throw RuntimeException(errorMessage) // Or any other exception
            }

            // Initialize ViewModel (this will trigger observeOdds)
            viewModel = OddsListViewModel(getSortedOddsStreamUseCase, triggerOddsUpdateUseCase)

            viewModel.uiState.test {

                // Emission 1: Default initial state
                var currentState = awaitItem()
                assertFalse(DEFAULT_INITIAL_STATE_SHOULD_NOT_BE_LOADING, currentState.isLoading)
                assertNull(DEFAULT_INITIAL_STATE_SHOULD_HAVE_NO_ERROR, currentState.error)

                // Advance dispatcher to allow onStart to execute
                testDispatcher.scheduler.advanceUntilIdle()

                // Emission 2: State from onStart (this should execute before the flow throws the exception)
                currentState = awaitItem()
                assertTrue(STATE_FROM_ON_START_SHOULD_BE_LOADING, currentState.isLoading)
                assertNull(STATE_FROM_ON_START_SHOULD_HAVE_NO_ERROR, currentState.error)

                // Advance dispatcher to allow the flow error to propagate and be caught
                testDispatcher.scheduler.advanceUntilIdle()

                // Emission 3: State from the outer .catch {} block in observeOdds
                currentState = awaitItem()
                assertFalse(
                    "$SHOULD_NOT_BE_LOADING_AFTER_FLOW_ERROR $ACTUAL $ERROR: ${currentState.error}",
                    currentState.isLoading
                )
                Assert.assertNotNull(
                    "$ERROR_MESSAGE_SHOULD_BE_PRESENT_ACTUAL_ERROR: ${currentState.error}",
                    currentState.error,
                )
                assertEquals(
                    ERROR_MESSAGE_DOES_NOT_MATCH_EXPECTED,
                    expectedUiError,
                    currentState.error
                )

                // Assert odds state if necessary (e.g., should be empty or retain previous)
                assertTrue(
                    "$ODDS_SHOULD_BE_EMPTY_AFTER_FLOW $ERROR $IF_THEY_ARE_CLEARED",
                    currentState.odds.isEmpty()
                )

                // Clean up
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `onUpdateOddsClicked sets loading calls use case then reflects new odds`() =
        runTest(testDispatcher) {
            val initialOdds = DataSource.createTestInitialOddsList()
            val updatedOdds = DataSource.createTestUpdatedOddsList()
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
                assertTrue(SHOULD_BE_LOADING_DURING_UPDATE, state.isLoading)
                // odds might still be the initial ones here

                // 4. triggerOddsUpdateUseCase is called (coAnswers block runs, emits to oddsFlow)
                // 5. observeOdds collects from oddsFlow, updates state with new odds, isLoading false
                state = awaitItem()
                assertFalse(SHOULD_NOT_BE_LOADING_AFTER_UPDATE_SUCCESS, state.isLoading)
                assertEquals(ODDS_SHOULD_BE_UPDATED, expectedUiUpdatedOdds, state.odds)
                assertNull(ERROR_SHOULD_BE_NULL, state.error)
                coVerify { triggerOddsUpdateUseCase() }

                // Clean up
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `onUpdateOddsClicked shows error if triggerOddsUpdateUseCase fails`() =
        runTest(testDispatcher) {
            val errorMessage = UPDATE_TRIGGER_FAILED
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
                assertFalse(SHOULD_NOT_BE_LOADING_AFTER_TRIGGER_FAILURE, state.isLoading)
                assertEquals("$UPDATE_FAILED: $errorMessage", state.error)
                // Odds should likely remain as they were before the failed trigger
                assertTrue(
                    ODDS_SHOULD_BE_WHAT_THEY_WERE_BEFORE_TRIGGER_FAILURE,
                    state.odds.isEmpty()
                )

                coVerify { triggerOddsUpdateUseCase() }

                // Clean up
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `onUpdateOddsClicked successful trigger but subsequent Data Flow fails`() =
        runTest(testDispatcher) {
            val initialOdds = DataSource.createTestInitialOddsList()
            val dataFlowErrorMessage = "$DATA $ERROR $AFTER_REFRESH_TRIGGER"

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
                    assertFalse("$SHOULD_NOT_BE_LOADING_AFTER_DATA_FLOW $ERROR", state.isLoading)
                    assertEquals("$DATA_STREAM_ERROR: $dataFlowErrorMessage", state.error)
                    // Odds might revert to what they were or be empty
                    assertEquals(
                        ODDS_SHOULD_REMAIN_FROM_LAST_GOOD_STATE_OR_BE_EMPTY,
                        initialOdds.map { it.toOddItemUiModel() }, state.odds
                    ) // Or emptyList() based on preference

                    // Clean up
                    cancelAndConsumeRemainingEvents()
                }
            }
        }

}
