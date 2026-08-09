package com.chalkak.recap.core.data.capture

import app.cash.turbine.test
import com.chalkak.recap.core.data.screenshot.image.ScreenshotImageStorage
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteCaptureThumbnailCacheTest {
    @TempDir
    lateinit var tempDir: File

    private val testDispatcher = StandardTestDispatcher()
    private val imageStorage = mockk<ScreenshotImageStorage>()
    private val okHttpClient = mockk<OkHttpClient>()
    private lateinit var cache: RemoteCaptureThumbnailCache

    @BeforeEach
    fun setUp() {
        every { imageStorage.clearStoredImages() } returns true
        every { imageStorage.deleteStoredImages(any()) } just Runs
        cache = RemoteCaptureThumbnailCache(
            imageStorage = imageStorage,
            okHttpClient = okHttpClient,
            cacheScope = CoroutineScope(SupervisorJob() + testDispatcher),
        )
    }

    @Test
    fun `resolveThumbnailSources returns local path for cache hit without download`() = runTest(testDispatcher) {
        val hitFile = File(tempDir, "1.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3))
        }
        every { imageStorage.buildThumbnailPath(1L) } returns hitFile

        val resolved = cache.resolveThumbnailSources(
            listOf(1L to "https://example.com/1.jpg"),
        )

        assertEquals(hitFile.absolutePath, resolved[1L])
        verify(exactly = 0) { okHttpClient.newCall(any()) }
    }

    @Test
    fun `resolveThumbnailSources returns null for miss and emits ready after prefetch`() = runTest(testDispatcher) {
        val missFile = File(tempDir, "2.jpg")
        every { imageStorage.buildThumbnailPath(2L) } returns missFile
        every { imageStorage.cacheThumbnailBytes(2L, any()) } answers {
            missFile.writeBytes(secondArg())
            missFile.absolutePath
        }
        stubSuccessfulDownload(byteArrayOf(9, 9, 9))

        cache.thumbnailReady.test {
            val resolved = cache.resolveThumbnailSources(
                listOf(2L to "https://example.com/2.jpg"),
            )
            assertNull(resolved[2L])

            advanceUntilIdle()

            assertEquals(
                CaptureThumbnailReady(captureId = 2L, localPath = missFile.absolutePath),
                awaitItem(),
            )
            ensureAllEventsConsumed()
        }
        verify(exactly = 1) { okHttpClient.newCall(any()) }
    }

    @Test
    fun `resolveThumbnailSources single-flights duplicate miss downloads`() = runTest(testDispatcher) {
        val missFile = File(tempDir, "3.jpg")
        every { imageStorage.buildThumbnailPath(3L) } returns missFile
        every { imageStorage.cacheThumbnailBytes(3L, any()) } answers {
            missFile.writeBytes(secondArg())
            missFile.absolutePath
        }
        stubSuccessfulDownload(byteArrayOf(4, 5))

        cache.resolveThumbnailSources(listOf(3L to "https://example.com/3.jpg"))
        cache.resolveThumbnailSources(listOf(3L to "https://example.com/3.jpg"))
        advanceUntilIdle()

        verify(exactly = 1) { okHttpClient.newCall(any()) }
    }

    @Test
    fun `clearAll before queued prefetch runs prevents write and emit`() = runTest(testDispatcher) {
        val missFile = File(tempDir, "4.jpg")
        every { imageStorage.buildThumbnailPath(4L) } returns missFile
        every { imageStorage.cacheThumbnailBytes(any(), any()) } answers {
            missFile.writeBytes(secondArg())
            missFile.absolutePath
        }
        stubSuccessfulDownload(byteArrayOf(1))

        cache.thumbnailReady.test {
            cache.resolveThumbnailSources(listOf(4L to "https://example.com/4.jpg"))
            cache.clearAll()
            advanceUntilIdle()
            expectNoEvents()
        }
        verify(exactly = 0) { imageStorage.cacheThumbnailBytes(any(), any()) }
        assertFalse(missFile.exists())
    }

    @Test
    fun `clearAll during download prevents write and emit`() = runTest(testDispatcher) {
        val missFile = File(tempDir, "5.jpg")
        every { imageStorage.buildThumbnailPath(5L) } returns missFile
        every { imageStorage.cacheThumbnailBytes(any(), any()) } answers {
            missFile.writeBytes(secondArg())
            missFile.absolutePath
        }
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } answers {
            val request = firstArg<Request>()
            every { call.execute() } answers {
                cache.clearAll()
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(byteArrayOf(7, 7).toResponseBody())
                    .build()
            }
            call
        }

        cache.thumbnailReady.test {
            cache.resolveThumbnailSources(listOf(5L to "https://example.com/5.jpg"))
            advanceUntilIdle()
            expectNoEvents()
        }
        verify(exactly = 0) { imageStorage.cacheThumbnailBytes(any(), any()) }
    }

    @Test
    fun `deleteCachedThumbnails during download prevents write and emit`() = runTest(testDispatcher) {
        val missFile = File(tempDir, "6.jpg")
        every { imageStorage.buildThumbnailPath(6L) } returns missFile
        every { imageStorage.cacheThumbnailBytes(any(), any()) } answers {
            missFile.writeBytes(secondArg())
            missFile.absolutePath
        }
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } answers {
            val request = firstArg<Request>()
            every { call.execute() } answers {
                cache.deleteCachedThumbnails(setOf(6L))
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(byteArrayOf(8, 8).toResponseBody())
                    .build()
            }
            call
        }

        cache.thumbnailReady.test {
            cache.resolveThumbnailSources(listOf(6L to "https://example.com/6.jpg"))
            advanceUntilIdle()
            expectNoEvents()
        }
        verify(exactly = 0) { imageStorage.cacheThumbnailBytes(any(), any()) }
    }

    private fun stubSuccessfulDownload(bytes: ByteArray) {
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } answers {
            val request = firstArg<Request>()
            every { call.execute() } answers {
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(bytes.toResponseBody())
                    .build()
            }
            call
        }
    }
}
