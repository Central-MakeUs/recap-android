package com.chalkak.recap.core.model

data class RecapCard(
    val id: String,
    val screenshotAssetId: String,
    val title: String,
    val summary: String,
    val tags: List<String>,
    val collectionIds: List<String>,
    val status: CardStatus,
    val createdAtMillis: Long,
    val memo: String? = null,
)
