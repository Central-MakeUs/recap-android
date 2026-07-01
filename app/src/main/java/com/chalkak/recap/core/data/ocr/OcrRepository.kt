package com.chalkak.recap.core.data.ocr

import com.chalkak.recap.core.data.LocalScreenshotDataSource
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.OcrCleanupRange
import com.chalkak.recap.core.model.OcrJob
import com.chalkak.recap.core.model.OcrJobStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

@Singleton
class OcrRepository @Inject constructor(
    private val screenshotDataSource: LocalScreenshotDataSource,
    private val ocrDao: OcrDao,
    private val workManager: WorkManager,
) {
    fun imagePermissionRequest(): Array<String> = screenshotDataSource.imagePermissionRequest()

    fun currentImageAccessLevel(): ImageAccessLevel = screenshotDataSource.currentImageAccessLevel()

    suspend fun countScreenshots(range: OcrCleanupRange): Int {
        return screenshotDataSource.countScreenshots(range)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLatestJob(): Flow<OcrJob?> {
        return ocrDao.observeLatestJob().flatMapLatest { job ->
            if (job == null) {
                flowOf(null)
            } else {
                ocrDao.observeResults(job.jobId).map { results ->
                    job.toDomain(results)
                }
            }
        }
    }

    suspend fun startOcr(range: OcrCleanupRange): String {
        val jobId = UUID.randomUUID().toString()
        val request = OneTimeWorkRequestBuilder<OcrWorker>()
            .setInputData(
                workDataOf(
                    OcrWorker.KEY_JOB_ID to jobId,
                    OcrWorker.KEY_RANGE to range.name,
                ),
            )
            .build()

        ocrDao.finishActiveJobs(
            activeStatuses = listOf(OcrJobStatus.Pending.name, OcrJobStatus.Running.name),
            status = OcrJobStatus.Cancelled.name,
            finishedAtMillis = System.currentTimeMillis(),
            errorMessage = null,
        )
        ocrDao.insertJob(
            OcrJobEntity(
                jobId = jobId,
                workId = request.id.toString(),
                range = range.name,
                status = OcrJobStatus.Pending.name,
                completedCount = 0,
                totalCount = 0,
                startedAtMillis = System.currentTimeMillis(),
                finishedAtMillis = null,
                errorMessage = null,
            ),
        )
        workManager.enqueueUniqueWork(
            OCR_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
        return jobId
    }

    private companion object {
        const val OCR_WORK_NAME = "onboarding_ocr_cleanup"
    }
}
