package com.chalkak.recap.core.data.ocr

import android.content.Context
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.chalkak.recap.core.data.entity.EntityTextExtractor
import com.chalkak.recap.core.data.LocalScreenshotDataSource
import com.chalkak.recap.core.model.OcrCleanupRange
import com.chalkak.recap.core.model.OcrJobStatus
import com.chalkak.recap.core.model.OcrTextBlock
import com.google.android.gms.tasks.Task
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltWorker
class OcrWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val ocrDao: OcrDao,
    private val screenshotDataSource: LocalScreenshotDataSource,
    private val entityTextExtractor: EntityTextExtractor,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val jobId = inputData.getString(KEY_JOB_ID) ?: return Result.failure()
        val range = inputData.getString(KEY_RANGE)
            ?.let { runCatching { OcrCleanupRange.valueOf(it) }.getOrNull() }
            ?: return Result.failure()

        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        return try {
            val images = screenshotDataSource.queryScreenshots(range)
            ocrDao.updateJobProgress(
                jobId = jobId,
                status = OcrJobStatus.Running.name,
                completedCount = 0,
                totalCount = images.size,
                errorMessage = null,
            )
            setProgress(workDataOf(KEY_COMPLETED to 0, KEY_TOTAL to images.size))

            images.forEachIndexed { index, image ->
                currentCoroutineContext().ensureActive()
                val inputImage = InputImage.fromFilePath(applicationContext, image.uri.toUri())
                val recognizedText = recognizer.process(inputImage).await()
                val entityAnnotationsJson = runCatching {
                    entityTextExtractor.extract(recognizedText.text).toEntityAnnotationsJson()
                }.getOrDefault(EmptyEntityAnnotationsJson)
                val completedCount = index + 1

                ocrDao.insertResult(
                    OcrResultEntity(
                        jobId = jobId,
                        imageUri = image.uri,
                        displayName = image.displayName,
                        rawText = recognizedText.text,
                        entityAnnotationsJson = entityAnnotationsJson,
                        rawTextBlocksJson = recognizedText.toOcrTextBlocks().toJson(),
                        sortIndex = index,
                    ),
                )
                ocrDao.updateJobProgress(
                    jobId = jobId,
                    status = OcrJobStatus.Running.name,
                    completedCount = completedCount,
                    totalCount = images.size,
                    errorMessage = null,
                )
                setProgress(workDataOf(KEY_COMPLETED to completedCount, KEY_TOTAL to images.size))
            }

            ocrDao.updateJobProgress(
                jobId = jobId,
                status = OcrJobStatus.Completed.name,
                completedCount = images.size,
                totalCount = images.size,
                errorMessage = null,
            )
            ocrDao.finishJob(
                jobId = jobId,
                status = OcrJobStatus.Completed.name,
                finishedAtMillis = System.currentTimeMillis(),
                errorMessage = null,
            )
            Result.success()
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) {
                ocrDao.finishJob(
                    jobId = jobId,
                    status = OcrJobStatus.Cancelled.name,
                    finishedAtMillis = System.currentTimeMillis(),
                    errorMessage = null,
                )
                throw throwable
            }
            ocrDao.finishJob(
                jobId = jobId,
                status = OcrJobStatus.Failed.name,
                finishedAtMillis = System.currentTimeMillis(),
                errorMessage = throwable::class.java.simpleName,
            )
            Result.failure()
        } finally {
            recognizer.close()
        }
    }

    companion object {
        const val KEY_JOB_ID = "job_id"
        const val KEY_RANGE = "range"
        const val KEY_COMPLETED = "completed"
        const val KEY_TOTAL = "total"
    }
}

private fun Text.toOcrTextBlocks(): List<OcrTextBlock> {
    return textBlocks.map { block ->
        OcrTextBlock(text = block.text)
    }
}

private fun List<EntityAnnotation>.toEntityAnnotationsJson(): String {
    val jsonArray = JSONArray()
    forEach { annotation ->
        jsonArray.put(
            JSONObject()
                .put("text", annotation.annotatedText)
                .put("start", annotation.start)
                .put("end", annotation.end)
                .put(
                    "entities",
                    JSONArray().apply {
                        annotation.entities.forEach { entity ->
                            put(
                                JSONObject()
                                    .put("type", entity.type)
                                    .put("raw", entity.toString()),
                            )
                        }
                    },
                ),
        )
    }
    return jsonArray.toString()
}

private suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { throwable ->
            continuation.resumeWithException(throwable)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}

private const val EmptyEntityAnnotationsJson = "[]"
