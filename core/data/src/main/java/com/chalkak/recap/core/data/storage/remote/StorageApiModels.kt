package com.chalkak.recap.core.data.storage.remote

import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.capture.remote.toDomain
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.model.storage.StorageType
import kotlinx.serialization.Serializable

@Serializable
data class StorageTypeResponseDto(
    val typeCode: CardTypeDto,
    val count: Long,
    val representativeTitles: List<String> = emptyList(),
)

typealias StorageTypesApiResponse = ApiResponseDto<List<StorageTypeResponseDto>>

fun StorageTypeResponseDto.toDomain() =
    StorageType(
        typeCode = typeCode.toDomain(),
        count = count,
        representativeTitles = representativeTitles,
    )
