package com.chalkak.recap.core.data.screenshot

enum class ScreenshotBackendMode {
    MOCK,
    REMOTE,
    ;

    companion object {
        fun fromStoredValue(value: String?): ScreenshotBackendMode {
            return entries.firstOrNull { it.name == value } ?: MOCK
        }
    }
}
