package com.chalkak.recap.core.model.user

data class AccountInfo(
    val platform: String,
    val createdAt: String,
)

data class DataSummary(
    val capturedCount: Long,
)

data class Usage(
    val usedCount: Int,
    val limit: Int,
    val remaining: Int,
    val resetAt: String,
)

data class ConsentStatus(
    val consented: Boolean,
    val consentedAt: String? = null,
)
