package com.chalkak.recap.core.data.home.remote

import com.chalkak.recap.core.data.capture.remote.CaptureSummaryResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.capture.remote.toDomain
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.home.HomeSummary
import com.chalkak.recap.core.model.home.TopType
import kotlinx.serialization.Serializable

@Serializable
data class TopTypeResponseDto(
    val typeCode: CardTypeDto,
    val count: Long,
    val representativeThumbnailUrl: String? = null,
)

@Serializable
data class HomeSummaryResponseDto(
    val recentCaptures: List<CaptureSummaryResponseDto> = emptyList(),
    val favorites: List<CaptureSummaryResponseDto> = emptyList(),
    val topTypes: List<TopTypeResponseDto> = emptyList(),
    val hasAnyCapture: Boolean = false,
)

typealias HomeSummaryApiResponse = ApiResponseDto<HomeSummaryResponseDto>

fun TopTypeResponseDto.toDomain() =
    TopType(
        typeCode = typeCode.toDomain(),
        count = count,
        representativeThumbnailUrl = representativeThumbnailUrl,
    )

fun HomeSummaryResponseDto.toDomain() =
    HomeSummary(
        recentCaptures = recentCaptures.map { it.toDomain() },
        favorites = favorites.map { it.toDomain() },
        topTypes = topTypes.map { it.toDomain() },
        hasAnyCapture = hasAnyCapture,
    )
