package com.betsson.interviewtest.data.dto


import com.betsson.interviewtest.domain.model.OddType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.UUID // Not strictly needed for assertions but good for context

private const val SHOULD_START_WITH_THE_CORRECT_PREFIX = "should start with the correct prefix"
private const val ID_SHOULD_START_WITH_THE_CORRECT_PREFIX =
    "ID $SHOULD_START_WITH_THE_CORRECT_PREFIX"
private const val ID_SHOULD_HAVE_CONTENT_AFTER_THE_PREFIX =
    "ID should have content after the prefix"
private const val THE_SUFFIX_OF_THE_ID = "The suffix of the ID"
private const val IS_NOT_A_VALID_UUID_STRING = "is not a valid UUID string."
private const val FOR_ODD_TYPE_OTHER = "for OddType.Other"
private const val IDS_FROM_TWO_SEPARATE_MAP_BET_TO_ODD_CALLS_SHOULD_BE_UNIQUE =
    "IDs from two separate mapBetToOdd calls should be unique"

class BetTest {

    @Test
    fun `mapBetToOdd - correctly maps all properties`() {
        val betType = OddType.TotalScore // Using a concrete OddType
        val betSellIn = 10
        val betOdds = 25
        val betImage = "http://example.com/image.png"

        val bet = Bet(
            type = betType,
            sellIn = betSellIn,
            odds = betOdds,
            image = betImage
        )

        val odd = bet.mapBetToOdd()

        // Assert basic property mapping
        assertEquals(betType.displayName, odd.name)
        assertEquals(betSellIn, odd.sellIn)
        assertEquals(betOdds, odd.oddsValue)
        assertEquals(betImage, odd.imageUrl)
    }

    @Test
    fun `mapBetToOdd - id generation format is correct`() {
        val betType = OddType.NumberOfFouls // Using another OddType
        val bet = Bet(
            type = betType,
            sellIn = 5,
            odds = 15,
            image = "image_url"
        )

        val odd = bet.mapBetToOdd()

        // Verify the prefix of the ID
        val expectedIdPrefix = betType.displayName
            .replace(" ", "_")
            .lowercase(Locale.getDefault()) + "_"

        assertTrue(ID_SHOULD_START_WITH_THE_CORRECT_PREFIX, odd.id.startsWith(expectedIdPrefix))

        // Verify that there's something after the prefix (the UUID part)
        assertTrue(ID_SHOULD_HAVE_CONTENT_AFTER_THE_PREFIX, odd.id.length > expectedIdPrefix.length)

        // Crude check for UUID-like structure (36 characters for a standard UUID string)
        // This is not foolproof for validating a UUID but good enough for this mapping test.
        val uuidPart = odd.id.substring(expectedIdPrefix.length)
        // A standard UUID string like "xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx" has 36 chars.
        // We can check if the length implies a UUID.
        // This might be too brittle if UUID.randomUUID().toString() has variable length (it doesn't typically)
        // A more robust check might involve trying to parse it as a UUID.
        try {
            UUID.fromString(uuidPart) // This will throw IllegalArgumentException if not a valid UUID string
        } catch (e: IllegalArgumentException) {
            throw AssertionError(
                "$THE_SUFFIX_OF_THE_ID '${uuidPart}' $IS_NOT_A_VALID_UUID_STRING",
                e
            )
        }
    }

    @Test
    fun `mapBetToOdd - generates unique IDs for different calls (due to UUID)`() {
        val betType = OddType.FirstGoalScorer
        val bet = Bet(
            type = betType,
            sellIn = 0,
            odds = 50,
            image = "another_image.jpg"
        )

        val odd1 = bet.mapBetToOdd()
        val odd2 = bet.mapBetToOdd() // Call mapping again on the same Bet instance

        // Names and other core data should be the same
        assertEquals(odd1.name, odd2.name)
        assertEquals(odd1.sellIn, odd2.sellIn)
        assertEquals(odd1.oddsValue, odd2.oddsValue)
        assertEquals(odd1.imageUrl, odd2.imageUrl)

        // IDs should be different due to UUID.randomUUID()
        assertNotEquals(
            IDS_FROM_TWO_SEPARATE_MAP_BET_TO_ODD_CALLS_SHOULD_BE_UNIQUE,
            odd1.id,
            odd2.id
        )

        // Both IDs should still conform to the expected prefix
        val expectedIdPrefix = betType.displayName
            .replace(" ", "_")
            .lowercase(Locale.getDefault()) + "_"
        assertTrue(
            "ID1 $SHOULD_START_WITH_THE_CORRECT_PREFIX",
            odd1.id.startsWith(expectedIdPrefix)
        )
        assertTrue(
            "ID2 $SHOULD_START_WITH_THE_CORRECT_PREFIX",
            odd2.id.startsWith(expectedIdPrefix)
        )
    }

    @Test
    fun `mapBetToOdd - handles OddType Other correctly`() {
        val customTypeName = "Custom Super Bet"
        val betType = OddType.Other(customTypeName)
        val betSellIn = 7
        val betOdds = 33
        val betImage = "custom_image.gif"

        val bet = Bet(
            type = betType,
            sellIn = betSellIn,
            odds = betOdds,
            image = betImage
        )

        val odd = bet.mapBetToOdd()

        assertEquals(customTypeName, odd.name) // Should use the actualName from OddType.Other
        assertEquals(betSellIn, odd.sellIn)
        assertEquals(betOdds, odd.oddsValue)
        assertEquals(betImage, odd.imageUrl)

        val expectedIdPrefix = customTypeName
            .replace(" ", "_")
            .lowercase(Locale.getDefault()) + "_"
        assertTrue(
            "ID $FOR_ODD_TYPE_OTHER $SHOULD_START_WITH_THE_CORRECT_PREFIX",
            odd.id.startsWith(expectedIdPrefix)
        )

        val uuidPart = odd.id.substring(expectedIdPrefix.length)
        try {
            UUID.fromString(uuidPart)
        } catch (e: IllegalArgumentException) {
            throw AssertionError(
                "$THE_SUFFIX_OF_THE_ID $FOR_ODD_TYPE_OTHER '${uuidPart}' $IS_NOT_A_VALID_UUID_STRING",
                e
            )
        }
    }
}
