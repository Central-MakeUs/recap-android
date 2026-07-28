package com.chalkak.recap.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RecapNavHostBackStackTest {

    @Test
    fun `removeLastIfNotRoot removes the current destination`() {
        val backStack = mutableListOf("root", "detail")

        assertEquals("detail", backStack.removeLastIfNotRoot())
        assertEquals(listOf("root"), backStack)
    }

    @Test
    fun `removeLastIfNotRoot preserves the root destination`() {
        val backStack = mutableListOf("root")

        assertNull(backStack.removeLastIfNotRoot())
        assertEquals(listOf("root"), backStack)
    }
}
