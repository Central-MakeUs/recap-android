package com.chalkak.recap.app

import androidx.navigation3.runtime.NavKey
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

    @Test
    fun `orphaned analysis destination restores a navigable home stack`() {
        val backStack = mutableListOf<NavKey>(AppRoute.MainTabs, AppRoute.OrganizeAnalysisStatus)

        backStack.removeOrphanedAnalysisStatus()

        assertEquals(listOf(AppRoute.MainTabs), backStack)
    }

    @Test
    fun `orphaned analysis cleanup preserves other restored destinations`() {
        val backStack = mutableListOf<NavKey>(
            AppRoute.MainTabs,
            AppRoute.Screenshot(17),
            AppRoute.OrganizeAnalysisStatus,
        )

        backStack.removeOrphanedAnalysisStatus()

        assertEquals(listOf(AppRoute.MainTabs, AppRoute.Screenshot(17)), backStack)
    }
}
