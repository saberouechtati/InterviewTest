package com.betsson.interviewtest.presentation.features.oddsList.components


import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.betsson.interviewtest.domain.model.OddItemUiModel
import com.betsson.interviewtest.presentation.theme.InterviewTestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OddItemRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleOddItemUiModel = OddItemUiModel(
        id = "sample-id-123",
        name = "FC Barcelona to Win",
        sellInText = "Expires in: 5 days",
        oddsValueText = "Odds: 1.85",
        imageUrl = "http://example.com/barca_logo.png"
    )

    @Test
    fun oddItemRow_displaysAllInformationCorrectly() {
        composeTestRule.setContent {
            // It's crucial to wrap your composable in your app's theme
            // or at least MaterialTheme if it uses Material components/typography.
            InterviewTestTheme { // Replace with your actual app theme if different
                OddItemRow(oddItem = sampleOddItemUiModel)
            }
        }

        // Assert name is displayed
        composeTestRule.onNodeWithText("FC Barcelona to Win").assertIsDisplayed()

        // Assert sellInText is displayed
        composeTestRule.onNodeWithText("Expires in: 5 days").assertIsDisplayed()

        // Assert oddsValueText is displayed
        composeTestRule.onNodeWithText("Odds: 1.85").assertIsDisplayed()

        // Assert image is present by checking its content description (which is oddItem.name)
        composeTestRule.onNodeWithContentDescription("FC Barcelona to Win").assertIsDisplayed()
        // As before, we're checking the AsyncImage Composable is present and configured,
        // not that the image bytes loaded correctly.
    }

    // You can add more tests for different OddItemUiModel states if relevant
    // e.g., very long text, missing image URL (how placeholder/error is handled by AsyncImage)
    // though testing Coil's placeholder/error directly is more of Coil's responsibility.
    // We mainly ensure our contentDescription is correct.

    @Test
    fun oddItemRow_withDifferentData_displaysCorrectly() {
        val anotherOddItem = OddItemUiModel(
            id = "sample-id-456",
            name = "Real Madrid to Score First",
            sellInText = "Sell In: 2",
            oddsValueText = "Current Odds: 2.10",
            imageUrl = "http://example.com/real_logo.png"
        )

        composeTestRule.setContent {
            InterviewTestTheme {
                OddItemRow(oddItem = anotherOddItem)
            }
        }

        composeTestRule.onNodeWithText("Real Madrid to Score First").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sell In: 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Current Odds: 2.10").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Real Madrid to Score First").assertIsDisplayed()
    }
}
