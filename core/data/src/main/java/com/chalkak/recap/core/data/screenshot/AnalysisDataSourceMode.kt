package com.chalkak.recap.core.data.screenshot

enum class AnalysisDataSourceMode {
    MOCK,
    REMOTE,
    ;

    companion object {
        fun fromStoredValue(value: String?): AnalysisDataSourceMode {
            return entries.firstOrNull { it.name == value } ?: MOCK
        }
    }
}
