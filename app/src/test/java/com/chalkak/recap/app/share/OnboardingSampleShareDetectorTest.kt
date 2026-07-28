package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.LocalImage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OnboardingSampleShareDetectorTest {
    private val packageName = "com.chalkak.recap"

    @Test
    fun `fileprovider onboarding sample uri is detected`() {
        val image = LocalImage(
            uri = "content://com.chalkak.recap.fileprovider/onboarding_share/onboarding_add_to_favorite_share.png",
            displayName = "onboarding_add_to_favorite_share.png",
            dateAddedMillis = 1L,
        )

        assertTrue(
            OnboardingSampleShareDetector.isOnboardingSampleShare(
                images = listOf(image),
                packageName = packageName,
            ),
        )
    }

    @Test
    fun `mediastore uri is not detected as sample`() {
        val image = LocalImage(
            uri = "content://media/external/images/media/12",
            displayName = "onboarding_add_to_favorite_share.png",
            dateAddedMillis = 1L,
        )

        assertFalse(
            OnboardingSampleShareDetector.isOnboardingSampleShare(
                images = listOf(image),
                packageName = packageName,
            ),
        )
    }

    @Test
    fun `other fileprovider file is not detected as sample`() {
        val image = LocalImage(
            uri = "content://com.chalkak.recap.fileprovider/onboarding_share/other.png",
            displayName = "other.png",
            dateAddedMillis = 1L,
        )

        assertFalse(
            OnboardingSampleShareDetector.isOnboardingSampleShare(
                images = listOf(image),
                packageName = packageName,
            ),
        )
    }

    @Test
    fun `empty accepted list is not a sample share`() {
        assertFalse(
            OnboardingSampleShareDetector.isOnboardingSampleShare(
                images = emptyList(),
                packageName = packageName,
            ),
        )
    }

    @Test
    fun `mixed sample and normal images is not a sample share`() {
        val sample = LocalImage(
            uri = "content://com.chalkak.recap.fileprovider/onboarding_share/onboarding_add_to_favorite_share.png",
            displayName = "onboarding_add_to_favorite_share.png",
            dateAddedMillis = 1L,
        )
        val other = LocalImage(
            uri = "content://media/external/images/media/12",
            displayName = "photo.jpg",
            dateAddedMillis = 2L,
        )

        assertFalse(
            OnboardingSampleShareDetector.isOnboardingSampleShare(
                images = listOf(sample, other),
                packageName = packageName,
            ),
        )
    }
}
