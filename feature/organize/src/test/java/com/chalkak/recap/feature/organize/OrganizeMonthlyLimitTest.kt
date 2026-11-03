package com.chalkak.recap.feature.organize

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrganizeMonthlyLimitTest {
    @Test
    fun `monthly limits preserve initial selection and restrict confirmation`() {
        for (remaining in listOf(0, 1, 19, 20, 50)) {
            val initial =
                OrganizeUiState(monthlyRemaining = remaining, selectedUris = listOf("one"))
            assertEquals(20, initial.selectionLimit)
            val confirmation = initial.copy(hasEnteredConfirmation = true)
            assertEquals(minOf(remaining, 20), confirmation.selectionLimit)
            assertEquals(remaining > 0, confirmation.canStartOrganizing)
            assertTrue(confirmation.canProceed)
        }
        assertFalse(OrganizeUiState(hasEnteredConfirmation = true).canStartOrganizing)
        assertTrue(
            OrganizeUiState(
                hasEnteredConfirmation = true,
                selectedUris = listOf("one")
            ).canStartOrganizing
        )
    }
}
