package com.chalkak.recap.core.data.backend

/**
 * Pure contract for Gradle `USE_MOCK_BACKEND` project property resolution.
 *
 * Keep `core/data/build.gradle.kts` in sync:
 * - [parseOverride] / [effectiveForDebug] apply to the debug BuildConfig field only.
 * - release (and app qa via release fallback) is always Remote via [effectiveForRelease].
 */
object UseMockBackendProperty {
    fun parseOverride(raw: String?): Boolean? =
        when (raw) {
            null -> null
            "true" -> true
            "false" -> false
            else -> error(
                "Invalid USE_MOCK_BACKEND='$raw'. Allowed values are 'true' or 'false'.",
            )
        }

    fun effectiveForDebug(override: Boolean?): Boolean = override ?: true

    fun effectiveForRelease(): Boolean = false
}
