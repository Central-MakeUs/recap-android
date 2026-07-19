package com.chalkak.recap.core.data.storage

import com.chalkak.recap.core.data.capture.remote.CaptureListResponseDto
import com.chalkak.recap.core.data.capture.remote.CaptureSummaryResponseDto
import com.chalkak.recap.core.data.capture.remote.CardTypeDto
import com.chalkak.recap.core.data.network.ApiErrorDto
import com.chalkak.recap.core.data.network.ApiResponseDto
import com.chalkak.recap.core.data.network.RemoteApiException
import com.chalkak.recap.core.data.network.RemoteNetworkException
import com.chalkak.recap.core.data.storage.remote.StorageApi
import com.chalkak.recap.core.data.storage.remote.StorageTypeResponseDto
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.storage.CaptureSort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StorageRepositoryTest {
    private val storageApi = mockk<StorageApi>()
    private lateinit var repository: StorageRepository

    @BeforeEach
    fun setUp() {
        repository = StorageRepository(storageApi = storageApi)
    }

    @Test
    fun `getTypes maps success response`() = runTest {
        coEvery { storageApi.getTypes() } returns ApiResponseDto(
            success = true,
            data = listOf(
                StorageTypeResponseDto(
                    typeCode = CardTypeDto.KNOWLEDGE,
                    count = 2L,
                    representativeTitles = listOf("a", "b"),
                ),
            ),
        )

        val result = repository.getTypes()

        assertEquals(ScreenshotContentType.KNOWLEDGE, result.getOrNull()?.single()?.typeCode)
        assertEquals(2L, result.getOrNull()?.single()?.count)
    }

    @Test
    fun `getTypeCaptures sends type code and sort query`() = runTest {
        coEvery {
            storageApi.getTypeCaptures(typeCode = "KNOWLEDGE", sort = "oldest")
        } returns ApiResponseDto(
            success = true,
            data = CaptureListResponseDto(
                count = 1,
                items = listOf(
                    CaptureSummaryResponseDto(
                        captureId = 7L,
                        title = "t",
                        summary = "s",
                        typeCode = CardTypeDto.KNOWLEDGE,
                        thumbnailUrl = null,
                        isFavorite = false,
                        organizedAt = "2026-07-19T00:00:00Z",
                    ),
                ),
            ),
        )

        val result = repository.getTypeCaptures(
            typeCode = ScreenshotContentType.KNOWLEDGE,
            sort = CaptureSort.Oldest,
        )

        assertEquals(1, result.getOrNull()?.count)
        assertEquals(7L, result.getOrNull()?.items?.single()?.captureId)
        coVerify(exactly = 1) {
            storageApi.getTypeCaptures(typeCode = "KNOWLEDGE", sort = "oldest")
        }
    }

    @Test
    fun `getFavorites maps server error`() = runTest {
        coEvery { storageApi.getFavorites() } returns ApiResponseDto(
            success = false,
            data = null,
            error = ApiErrorDto(code = "INVALID_INPUT", message = "bad"),
        )

        val result = repository.getFavorites()

        val error = result.exceptionOrNull() as RemoteApiException
        assertEquals("INVALID_INPUT", error.code)
    }

    @Test
    fun `getEtc maps network failure`() = runTest {
        coEvery { storageApi.getEtc(any()) } throws IOException("offline")

        val result = repository.getEtc()

        assertTrue(result.exceptionOrNull() is RemoteNetworkException)
    }
}
