package com.chalkak.recap.core.data.screenshot.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screenshot_cards")
data class ScreenshotCardEntity(
    @PrimaryKey val captureId: Long,
    val sourceImageUri: String?,
    val storedImagePath: String?,
    val thumbnailPath: String?,
    val title: String,
    val summary: String,
    val body: String,
    val typeCode: String,
    val originalImageUrl: String,
    val isFavorite: Boolean,
    val organizedAtMillis: Long,
    val updatedAtMillis: Long,
)
