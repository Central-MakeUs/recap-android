package com.chalkak.recap.feature.settings.notification

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
