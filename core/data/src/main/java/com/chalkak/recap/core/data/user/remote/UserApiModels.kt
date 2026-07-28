package com.chalkak.recap.core.data.user.remote

import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.DataSummary
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

typealias AccountInfoApiResponse = ApiResponseDto<AccountInfoResponseDto>
typealias DataSummaryApiResponse = ApiResponseDto<DataSummaryResponseDto>

fun AccountInfoResponseDto.toDomain() =
    AccountInfo(
        platform = platform,
        createdAt = createdAt,
    )

fun DataSummaryResponseDto.toDomain() =
    DataSummary(
        capturedCount = capturedCount,
    )
