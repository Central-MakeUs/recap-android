package com.chalkak.recap.core.data.ocr

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chalkak.recap.core.model.OcrCleanupRange
import com.chalkak.recap.core.model.OcrImageResult
import com.chalkak.recap.core.model.OcrJob
import com.chalkak.recap.core.model.OcrJobStatus

@Entity(tableName = "ocr_jobs")
data class OcrJobEntity(
    @PrimaryKey val jobId: String,
    val workId: String,
    val range: String,
    val status: String,
    val completedCount: Int,
    val totalCount: Int,
    val startedAtMillis: Long,
    val finishedAtMillis: Long?,
    val errorMessage: String?,
)

@Entity(tableName = "ocr_results")
data class OcrResultEntity(
    @PrimaryKey(autoGenerate = true) val resultId: Long = 0,
    val jobId: String,
    val imageUri: String,
    val displayName: String,
    val rawText: String,
    val sortIndex: Int,
)

fun OcrJobEntity.toDomain(results: List<OcrResultEntity>): OcrJob {
    return OcrJob(
        jobId = jobId,
        workId = workId,
        range = OcrCleanupRange.valueOf(range),
        status = OcrJobStatus.valueOf(status),
        completedCount = completedCount,
        totalCount = totalCount,
        startedAtMillis = startedAtMillis,
        finishedAtMillis = finishedAtMillis,
        errorMessage = errorMessage,
        results = results.map { it.toDomain() },
    )
}

fun OcrResultEntity.toDomain(): OcrImageResult {
    return OcrImageResult(
        imageUri = imageUri,
        displayName = displayName,
        rawText = rawText,
        sortIndex = sortIndex,
    )
}
