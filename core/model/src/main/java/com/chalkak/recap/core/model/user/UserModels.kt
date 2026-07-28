package com.chalkak.recap.core.model.user

data class AccountInfo(
    val platform: String,
    val createdAt: String,
)

data class DataSummary(
    val capturedCount: Long,
)
