package com.chalkak.recap.core.design.animation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * [RecapSceneTransitionPlanner]로 대체된 레이어 불변식의 회귀 가드.
 * 상세 sequence는 [RecapSceneTransitionPlannerTest]를 본다.
 */
class RecapSceneZIndexTrackerTest {

    @Test
    fun `consecutive pops keep each incoming scene below the outgoing scene`() {
        val planner = RecapSceneTransitionPlanner()
        planner.begin("first", "second", RecapNavigationKind.Forward)
        planner.onIdle("second")
        planner.begin("second", "third", RecapNavigationKind.Forward)
        planner.onIdle("third")

        assertEquals(
            1f,
            planner.begin("third", "second", RecapNavigationKind.Pop).targetContentZIndex,
        )
        planner.onIdle("second")
        assertEquals(
            0f,
            planner.begin("second", "first", RecapNavigationKind.Pop).targetContentZIndex,
        )
    }

    @Test
    fun `consecutive forwards keep each incoming scene above the outgoing scene`() {
        val planner = RecapSceneTransitionPlanner()

        assertEquals(
            1f,
            planner.begin("first", "second", RecapNavigationKind.Forward).targetContentZIndex,
        )
        planner.onIdle("second")
        assertEquals(
            2f,
            planner.begin("second", "third", RecapNavigationKind.Forward).targetContentZIndex,
        )
    }

    @Test
    fun `cancel then retarget does not reuse cancelled target as top layer`() {
        val planner = RecapSceneTransitionPlanner()
        planner.begin("first", "second", RecapNavigationKind.Forward)
        planner.cancelTo("first")

        val retarget = planner.begin("first", "third", RecapNavigationKind.Forward)
        assertTrue(retarget.targetContentZIndex > planner.zIndexOf("first")!!)
        assertEquals(null, planner.zIndexOf("second"))
    }
}
