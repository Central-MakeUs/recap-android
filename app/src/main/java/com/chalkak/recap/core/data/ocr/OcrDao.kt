package com.chalkak.recap.core.data.ocr

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OcrDao {
    @Query("SELECT * FROM ocr_jobs ORDER BY startedAtMillis DESC LIMIT 1")
    fun observeLatestJob(): Flow<OcrJobEntity?>

    @Query("SELECT * FROM ocr_results WHERE jobId = :jobId ORDER BY sortIndex ASC")
    fun observeResults(jobId: String): Flow<List<OcrResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: OcrJobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: OcrResultEntity)

    @Query(
        """
        UPDATE ocr_jobs
        SET status = :status,
            totalCount = :totalCount,
            completedCount = :completedCount,
            errorMessage = :errorMessage
        WHERE jobId = :jobId
        """,
    )
    suspend fun updateJobProgress(
        jobId: String,
        status: String,
        completedCount: Int,
        totalCount: Int,
        errorMessage: String?,
    )

    @Query(
        """
        UPDATE ocr_jobs
        SET status = :status,
            finishedAtMillis = :finishedAtMillis,
            errorMessage = :errorMessage
        WHERE jobId = :jobId
        """,
    )
    suspend fun finishJob(
        jobId: String,
        status: String,
        finishedAtMillis: Long?,
        errorMessage: String?,
    )

    @Query(
        """
        UPDATE ocr_jobs
        SET status = :status,
            finishedAtMillis = :finishedAtMillis,
            errorMessage = :errorMessage
        WHERE status IN (:activeStatuses)
        """,
    )
    suspend fun finishActiveJobs(
        activeStatuses: List<String>,
        status: String,
        finishedAtMillis: Long,
        errorMessage: String?,
    )
}
