package com.chalkak.recap.core.data.screenshot.persistence

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "screenshot_cards")
data class ScreenshotCardEntity(
    @PrimaryKey val imageId: String,
    val sourceImageUri: String?,
    val storedImagePath: String?,
    val thumbnailPath: String?,
    val title: String,
    val summary: String,
    val body: String,
    val primaryContentType: String,
    val confidence: String,
    val isFavorite: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Entity(
    tableName = "screenshot_key_fields",
    foreignKeys = [
        ForeignKey(
            entity = ScreenshotCardEntity::class,
            parentColumns = ["imageId"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["imageId"])],
)
data class ScreenshotKeyFieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageId: String,
    val label: String,
    val value: String,
    val displayPriority: Int,
    val isSensitive: Boolean,
)

data class ScreenshotCardWithKeyFields(
    @Embedded val card: ScreenshotCardEntity,
    @Relation(
        parentColumn = "imageId",
        entityColumn = "imageId",
    )
    val keyFields: List<ScreenshotKeyFieldEntity>,
)
