package com.chalkak.recap.core.data.capture

import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber

@Singleton
class RemoteCaptureThumbnailCache @Inject constructor(
    private val imageStorage: ScreenshotImageStorage,
    private val okHttpClient: OkHttpClient,
) {
    fun resolveLocalPath(captureId: Long): String? {
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
        return download(captureId, remoteUrl) ?: remoteUrl
    }

    suspend fun resolveThumbnailSources(
        items: List<Pair<Long, String?>>,
    ): Map<Long, String?> {
        return items.associate { (captureId, remoteUrl) ->
            captureId to resolveThumbnailSource(captureId, remoteUrl)
        }
    }

    fun deleteCachedThumbnails(captureIds: Set<Long>) {
        if (captureIds.isEmpty()) {
            return
        }
        imageStorage.deleteStoredImages(captureIds)
    }

    private suspend fun download(
        captureId: Long,
        remoteUrl: String,
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(remoteUrl).get().build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Timber.w(
                            "Failed to download thumbnail captureId=%s http=%s",
                            captureId,
                            response.code,
                        )
                        return@withContext null
                    }
                    val bytes = response.body.bytes()
                    if (bytes.isEmpty()) {
                        return@withContext null
                    }
                    imageStorage.cacheThumbnailBytes(captureId, bytes)
                }
            } catch (ioe: IOException) {
                Timber.w(ioe, "Failed to download thumbnail captureId=%s", captureId)
                null
            }
        }
}
