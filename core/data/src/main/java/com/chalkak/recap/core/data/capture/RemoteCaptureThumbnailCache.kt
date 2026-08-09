package com.chalkak.recap.core.data.capture

import androidx.annotation.VisibleForTesting
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

@Singleton
class RemoteCaptureThumbnailCache @VisibleForTesting constructor(
    private val imageStorage: ScreenshotImageStorage,
    private val okHttpClient: OkHttpClient,
    private val cacheScope: CoroutineScope,
) : CaptureThumbnailUpdates {
    @Inject
    constructor(
        imageStorage: ScreenshotImageStorage,
        okHttpClient: OkHttpClient,
    ) : this(
        imageStorage = imageStorage,
        okHttpClient = okHttpClient,
        cacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    )

    private val _thumbnailReady = MutableSharedFlow<CaptureThumbnailReady>(
        extraBufferCapacity = 64,
    )
    override val thumbnailReady: SharedFlow<CaptureThumbnailReady> = _thumbnailReady.asSharedFlow()

    private val downloadSemaphore = Semaphore(MAX_PARALLEL_DOWNLOADS)
    private val inFlight = mutableMapOf<Long, Deferred<String?>>()
    private val invalidatedIds = ConcurrentHashMap.newKeySet<Long>()
    private val cacheGeneration = AtomicLong(0L)

    override fun resolveLocalPath(captureId: Long): String? {
        val file = imageStorage.buildThumbnailPath(captureId)
        return file.takeIf { it.exists() && it.length() > 0L }?.absolutePath
    }

    suspend fun resolveThumbnailSource(
        captureId: Long,
        remoteUrl: String?,
    ): String? {
        resolveLocalPath(captureId)?.let { return it }
        if (remoteUrl.isNullOrBlank()) {
            return null
        }
        invalidatedIds.remove(captureId)
        return ensureDownload(captureId, remoteUrl).await() ?: remoteUrl
    }

    fun resolveThumbnailSources(
        items: List<Pair<Long, String?>>,
    ): Map<Long, String?> {
        return items.associate { (captureId, remoteUrl) ->
            val localPath = resolveLocalPath(captureId)
            when {
                localPath != null -> captureId to localPath
                remoteUrl.isNullOrBlank() -> captureId to null
                else -> {
                    invalidatedIds.remove(captureId)
                    ensureDownload(captureId, remoteUrl)
                    captureId to null
                }
            }
        }
    }

    fun deleteCachedThumbnails(captureIds: Set<Long>) {
        if (captureIds.isEmpty()) {
            return
        }
        invalidatedIds.addAll(captureIds)
        cancelInFlight(captureIds)
        imageStorage.deleteStoredImages(captureIds)
    }

    /**
     * in-flight prefetch를 취소하고 디스크 images/thumbnails를 비운다.
     *
     * @return 디스크 파일까지 완전히 지웠으면 true
     */
    fun clearAll(): Boolean {
        cacheGeneration.incrementAndGet()
        invalidatedIds.clear()
        cancelAllInFlight()
        return imageStorage.clearStoredImages()
    }

    private fun ensureDownload(
        captureId: Long,
        remoteUrl: String,
    ): Deferred<String?> {
        synchronized(inFlight) {
            inFlight[captureId]?.let { return it }
            val enqueuedGeneration = cacheGeneration.get()
            val deferred = cacheScope.async {
                downloadSemaphore.withPermit {
                    resolveLocalPath(captureId)?.let { return@withPermit it }
                    if (isInvalidated(captureId, enqueuedGeneration)) {
                        return@withPermit null
                    }
                    val path = download(
                        captureId = captureId,
                        remoteUrl = remoteUrl,
                        isStillValid = { !isInvalidated(captureId, enqueuedGeneration) },
                    )
                    if (path == null || isInvalidated(captureId, enqueuedGeneration)) {
                        if (path != null) {
                            imageStorage.deleteStoredImages(setOf(captureId))
                        }
                        return@withPermit null
                    }
                    _thumbnailReady.emit(CaptureThumbnailReady(captureId, path))
                    path
                }
            }
            inFlight[captureId] = deferred
            deferred.invokeOnCompletion {
                synchronized(inFlight) {
                    if (inFlight[captureId] === deferred) {
                        inFlight.remove(captureId)
                    }
                }
            }
            return deferred
        }
    }

    private fun cancelInFlight(captureIds: Set<Long>) {
        synchronized(inFlight) {
            captureIds.forEach { captureId ->
                inFlight.remove(captureId)?.cancel()
            }
        }
    }

    private fun cancelAllInFlight() {
        synchronized(inFlight) {
            inFlight.values.forEach { deferred -> deferred.cancel() }
            inFlight.clear()
        }
    }

    private fun isInvalidated(
        captureId: Long,
        generation: Long,
    ): Boolean {
        return generation != cacheGeneration.get() || captureId in invalidatedIds
    }

    private fun download(
        captureId: Long,
        remoteUrl: String,
        isStillValid: () -> Boolean,
    ): String? =
        try {
            val request = Request.Builder().url(remoteUrl).get().build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.w(
                        "Failed to download thumbnail captureId=%s http=%s",
                        captureId,
                        response.code,
                    )
                    return null
                }
                val bytes = response.body.bytes()
                if (bytes.isEmpty()) {
                    return null
                }
                if (!isStillValid()) {
                    return null
                }
                val path = imageStorage.cacheThumbnailBytes(captureId, bytes)
                if (path != null && !isStillValid()) {
                    imageStorage.deleteStoredImages(setOf(captureId))
                    return null
                }
                path
            }
        } catch (ioe: IOException) {
            Timber.w(ioe, "Failed to download thumbnail captureId=%s", captureId)
            null
        }

    private companion object {
        const val MAX_PARALLEL_DOWNLOADS = 4
    }
}
