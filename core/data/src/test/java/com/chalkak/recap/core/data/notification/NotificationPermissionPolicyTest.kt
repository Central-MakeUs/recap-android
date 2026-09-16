package com.chalkak.recap.core.data.notification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NotificationPermissionPolicyTest {
    @Test
    fun `first request opens permission dialog`() {
        val destination = resolveNotificationPermissionRequestDestination(
            hasRequestedPermission = false,
            shouldShowRequestRationale = false,
        )

        assertEquals(
            NotificationPermissionRequestDestination.PermissionDialog,
            destination,
        )
    }

    @Test
    fun `allowed retry opens permission dialog`() {
        val destination = resolveNotificationPermissionRequestDestination(
            hasRequestedPermission = true,
            shouldShowRequestRationale = true,
        )

        assertEquals(
            NotificationPermissionRequestDestination.PermissionDialog,
            destination,
        )
    }

    @Test
    fun `unavailable request opens application settings`() {
        val destination = resolveNotificationPermissionRequestDestination(
            hasRequestedPermission = true,
            shouldShowRequestRationale = false,
        )

        assertEquals(
            NotificationPermissionRequestDestination.ApplicationSettings,
            destination,
        )
    }

    @Test
    fun `prompt shows when app preference is off regardless of system permission state`() {
        assertTrue(
            shouldShowOrganizeNotificationPermissionPrompt(
                organizeCompleteNotificationEnabled = false,
            ),
        )
        assertFalse(
            shouldShowOrganizeNotificationPermissionPrompt(
                organizeCompleteNotificationEnabled = true,
            ),
        )
    }
}
