package com.chalkak.recap.core.data.screenshot.permission

import com.chalkak.recap.core.model.ImageAccessLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ImagePermissionPolicyTest {
    @Test
    fun `denied access requests permission dialog before the first request`() {
        val destination = resolveImagePermissionRequestDestination(
            accessLevel = ImageAccessLevel.Denied,
            hasRequestedPermission = false,
            shouldShowRequestRationale = false,
        )

        assertEquals(ImagePermissionRequestDestination.PermissionDialog, destination)
    }

    @Test
    fun `denied access requests permission dialog when the system allows another request`() {
        val destination = resolveImagePermissionRequestDestination(
            accessLevel = ImageAccessLevel.Denied,
            hasRequestedPermission = true,
            shouldShowRequestRationale = true,
        )

        assertEquals(ImagePermissionRequestDestination.PermissionDialog, destination)
    }

    @Test
    fun `selected access attempts full permission dialog before the first upgrade request`() {
        val destination = resolveImagePermissionRequestDestination(
            accessLevel = ImageAccessLevel.Selected,
            hasRequestedPermission = false,
            shouldShowRequestRationale = false,
        )

        assertEquals(ImagePermissionRequestDestination.PermissionDialog, destination)
    }

    @Test
    fun `selected access requests full permission dialog when the system allows another request`() {
        val destination = resolveImagePermissionRequestDestination(
            accessLevel = ImageAccessLevel.Selected,
            hasRequestedPermission = true,
            shouldShowRequestRationale = true,
        )

        assertEquals(ImagePermissionRequestDestination.PermissionDialog, destination)
    }

    @Test
    fun `selected access opens settings when the full permission dialog is unavailable`() {
        val destination = resolveImagePermissionRequestDestination(
            accessLevel = ImageAccessLevel.Selected,
            hasRequestedPermission = true,
            shouldShowRequestRationale = false,
        )

        assertEquals(ImagePermissionRequestDestination.ApplicationSettings, destination)
    }

    @Test
    fun `denied access opens settings when the system cannot show another request`() {
        val destination = resolveImagePermissionRequestDestination(
            accessLevel = ImageAccessLevel.Denied,
            hasRequestedPermission = true,
            shouldShowRequestRationale = false,
        )

        assertEquals(ImagePermissionRequestDestination.ApplicationSettings, destination)
    }

    @Test
    fun `full access opens settings`() {
        val destination = resolveImagePermissionRequestDestination(
            accessLevel = ImageAccessLevel.Full,
            hasRequestedPermission = false,
            shouldShowRequestRationale = false,
        )

        assertEquals(ImagePermissionRequestDestination.ApplicationSettings, destination)
    }
}
