package com.chalkak.recap.feature.collection

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionTypeGridColumnsTest {

    @Test
    fun `uses 2 columns below three-column floor`() {
        assertEquals(2, collectionTypeGridColumns(347.dp))
    }

    @Test
    fun `uses 3 columns from three-column floor through typical phone width`() {
        assertEquals(3, collectionTypeGridColumns(348.dp))
        assertEquals(3, collectionTypeGridColumns(360.dp))
        assertEquals(3, collectionTypeGridColumns(492.dp))
    }

    @Test
    fun `uses 4 columns from four-column floor`() {
        assertEquals(4, collectionTypeGridColumns(493.dp))
        assertEquals(4, collectionTypeGridColumns(700.dp))
    }
}
