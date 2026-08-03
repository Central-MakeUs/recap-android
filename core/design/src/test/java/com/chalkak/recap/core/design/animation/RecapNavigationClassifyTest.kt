package com.chalkak.recap.core.design.animation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecapNavigationClassifyTest {

    @Test
    fun `root change is replace`() {
        assertEquals(
            RecapNavigationKind.Replace,
            classifyRecapNavigation(listOf("Onboarding"), listOf("Main")),
        )
    }

    @Test
    fun `growing stack is forward`() {
        assertEquals(
            RecapNavigationKind.Forward,
            classifyRecapNavigation(listOf("Main"), listOf("Main", "Settings")),
        )
    }

    @Test
    fun `shrinking stack is pop`() {
        assertEquals(
            RecapNavigationKind.Pop,
            classifyRecapNavigation(listOf("Main", "Settings"), listOf("Main")),
        )
    }

    @Test
    fun `empty side is replace`() {
        assertEquals(
            RecapNavigationKind.Replace,
            classifyRecapNavigation(emptyList(), listOf("Main")),
        )
    }
}
