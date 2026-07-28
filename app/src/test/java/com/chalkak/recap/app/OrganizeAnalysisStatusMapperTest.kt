package com.chalkak.recap.app

import com.chalkak.recap.feature.organize.OrganizeAnalysisStatusUiState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class OrganizeAnalysisStatusMapperTest {

    @Test
    fun `hidden status keeps the last visible status`() {
        val previous = OrganizeAnalysisStatusUiState.Success(successCount = 5)

        val result = retainLastVisibleAnalysisStatus(
            previous = previous,
            current = OrganizeAnalysisStatusUiState.Hidden,
        )

        assertEquals(previous, result)
    }

    @Test
    fun `visible status replaces the last visible status`() {
        val current = OrganizeAnalysisStatusUiState.Progress(progress = 0.5f)

        val result = retainLastVisibleAnalysisStatus(
            previous = OrganizeAnalysisStatusUiState.Success(successCount = 5),
            current = current,
        )

        assertEquals(current, result)
    }

    @Test
    fun `hidden initial status remains absent`() {
        val result = retainLastVisibleAnalysisStatus(
            previous = null,
            current = OrganizeAnalysisStatusUiState.Hidden,
        )

        assertNull(result)
    }
}
