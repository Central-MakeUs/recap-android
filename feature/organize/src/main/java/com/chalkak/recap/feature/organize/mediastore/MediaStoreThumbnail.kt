package com.chalkak.recap.feature.organize.mediastore

import android.content.ContentResolver
import android.net.Uri
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.util.Size
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import timber.log.Timber

/**
 * MediaStore 시스템 썸네일 로드용 Coil 모델.
 * [sizePx]는 [ContentResolver.loadThumbnail]에 넘길 목표 한 변 픽셀이다.
 */
data class MediaStoreThumbnail(
    val uri: Uri,
    val sizePx: Int,
)

internal object MediaStoreThumbnailKeyer : Keyer<MediaStoreThumbnail> {
    override fun key(data: MediaStoreThumbnail, options: Options): String {
        return "media-thumb:${data.uri}:${data.sizePx}"
    }
}

internal class MediaStoreThumbnailFetcher(
    private val contentResolver: ContentResolver,
    private val data: MediaStoreThumbnail,
    private val options: Options,
    private val imageLoader: ImageLoader,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val sizePx = data.sizePx.coerceAtLeast(1)
        val signal = CancellationSignal()
        val cancelHandle = currentCoroutineContext()[Job]?.invokeOnCompletion {
            signal.cancel()
        }
        try {
            currentCoroutineContext().ensureActive()
            val bitmap = contentResolver.loadThumbnail(
                data.uri,
                Size(sizePx, sizePx),
                signal,
            )
            currentCoroutineContext().ensureActive()
            return ImageFetchResult(
                image = bitmap.asImage(),
                isSampled = true,
                dataSource = DataSource.DISK,
            )
        } catch (cancelled: OperationCanceledException) {
            throw CancellationException("MediaStore thumbnail cancelled", cancelled)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Timber.w(
                error,
                "Failed to load MediaStore thumbnail: uri=%s sizePx=%d",
                data.uri,
                sizePx,
            )
            return fetchOriginalUri(error)
        } finally {
            cancelHandle?.dispose()
        }
    }

    private suspend fun fetchOriginalUri(originalError: Exception): FetchResult {
        val mapped = imageLoader.components.map(data.uri, options)
        val fetcherResult = imageLoader.components.newFetcher(mapped, options, imageLoader)
            ?: throw originalError
        val (fetcher) = fetcherResult
        return fetcher.fetch() ?: throw originalError
    }

    class Factory : Fetcher.Factory<MediaStoreThumbnail> {
        override fun create(
            data: MediaStoreThumbnail,
            options: Options,
            imageLoader: ImageLoader,
        ): Fetcher {
            return MediaStoreThumbnailFetcher(
                contentResolver = options.context.contentResolver,
                data = data,
                options = options,
                imageLoader = imageLoader,
            )
        }
    }
}
