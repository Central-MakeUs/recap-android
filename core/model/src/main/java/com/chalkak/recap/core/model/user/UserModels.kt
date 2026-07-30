package com.chalkak.recap.core.model.user

data class AccountInfo(
    val platform: String,
    val createdAt: String,
)

data class DataSummary(
    val capturedCount: Long,
)

data class ConsentStatus(
    val consented: Boolean,
    val consentedAt: String? = null,
)
