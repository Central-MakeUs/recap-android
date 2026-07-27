package com.chalkak.recap.app.share

import com.chalkak.recap.core.model.LocalImage

internal object OnboardingSampleShareDetector {
    const val ShareFileName = "onboarding_add_to_favorite_share.png"
    const val SharePathSegment = "onboarding_share"

    fun isOnboardingSampleShare(
        images: List<LocalImage>,
        packageName: String,
    ): Boolean {
        if (images.isEmpty()) {
            return false
        }
        return images.all { image -> isOnboardingSampleImage(image, packageName) }
    }

    fun isOnboardingSampleImage(
        image: LocalImage,
        packageName: String,
    ): Boolean {
        val parts = parseContentUriParts(image.uri) ?: return false
        val expectedAuthority = "$packageName.fileprovider"
        if (parts.authority != expectedAuthority) {
            return false
        }
        val nameMatches = image.displayName == ShareFileName ||
            parts.lastPathSegment == ShareFileName ||
            parts.path.endsWith("/$ShareFileName")
        val pathMatches = parts.path.contains("/$SharePathSegment/") ||
            parts.path.endsWith("/$SharePathSegment")
        return nameMatches && pathMatches
    }

    private fun parseContentUriParts(uriString: String): ContentUriParts? {
        if (!uriString.startsWith("content://")) {
            return null
        }
        val rest = uriString.removePrefix("content://")
        val slashIndex = rest.indexOf('/')
        if (slashIndex < 0) {
            return ContentUriParts(
                authority = rest,
                path = "",
                lastPathSegment = null,
            )
        }
        val authority = rest.substring(0, slashIndex)
        val path = rest.substring(slashIndex)
        val lastPathSegment = path.substringAfterLast('/').takeIf { segment ->
            segment.isNotEmpty()
        }
        return ContentUriParts(
            authority = authority,
            path = path,
            lastPathSegment = lastPathSegment,
        )
    }

    private data class ContentUriParts(
        val authority: String,
        val path: String,
        val lastPathSegment: String?,
    )
}
