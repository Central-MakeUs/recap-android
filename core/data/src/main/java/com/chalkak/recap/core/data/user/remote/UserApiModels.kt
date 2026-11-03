package com.chalkak.recap.core.data.user.remote

import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import com.chalkak.recap.core.model.user.Usage
import kotlinx.serialization.Serializable

@Serializable
data class AccountInfoResponseDto(
    val platform: String,
    val createdAt: String,
)

@Serializable
data class DataSummaryResponseDto(
    val capturedCount: Long,
)

@Serializable
data class UsageResponseDto(
    val usedCount: Int,
    val limit: Int,
    val remaining: Int,
    val resetAt: String,
)

@Serializable
data class ConsentStatusResponseDto(
    val consented: Boolean,
    val consentedAt: String? = null,
)

typealias AccountInfoApiResponse = ApiResponseDto<AccountInfoResponseDto>
typealias DataSummaryApiResponse = ApiResponseDto<DataSummaryResponseDto>
typealias UsageApiResponse = ApiResponseDto<UsageResponseDto>
typealias ConsentStatusApiResponse = ApiResponseDto<ConsentStatusResponseDto>

fun AccountInfoResponseDto.toDomain() =
    AccountInfo(
        platform = platform,
        createdAt = createdAt,
    )

fun DataSummaryResponseDto.toDomain() =
    DataSummary(
        capturedCount = capturedCount,
    )

fun UsageResponseDto.toDomain() =
    Usage(
        usedCount = usedCount,
        limit = limit,
        remaining = remaining,
        resetAt = resetAt,
    )

fun ConsentStatusResponseDto.toDomain() =
    ConsentStatus(
        consented = consented,
        consentedAt = consentedAt,
    )
