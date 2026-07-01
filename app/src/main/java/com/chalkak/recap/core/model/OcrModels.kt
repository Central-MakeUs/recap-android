package com.chalkak.recap.core.model

data class OcrJob(
    val jobId: String,
    val workId: String,
    val range: OcrCleanupRange,
    val status: OcrJobStatus,
    val completedCount: Int,
    val totalCount: Int,
    val startedAtMillis: Long,
    val finishedAtMillis: Long?,
    val errorMessage: String?,
    val results: List<OcrImageResult>,
) {
    val progress: Float
        get() = if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()

    val isRunning: Boolean
        get() = status == OcrJobStatus.Pending || status == OcrJobStatus.Running

    val isCompleted: Boolean
        get() = status == OcrJobStatus.Completed
}

enum class OcrCleanupRange(
    val days: Long,
) {
    Last7Days(days = 7),
    Last30Days(days = 30),
    Last90Days(days = 90),
}

enum class OcrJobStatus {
    Pending,
    Running,
    Completed,
    Failed,
    Cancelled,
}

data class OcrImageResult(
    val imageUri: String,
    val displayName: String,
    val rawText: String,
    val sortIndex: Int,
)
