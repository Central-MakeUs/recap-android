package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.data.UserPreferencesRepository
import com.chalkak.recap.core.data.capture.RemoteCaptureChangeNotifier
import com.chalkak.recap.core.data.screenshot.backend.MockScreenshotDataResetter
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardRepository
import com.chalkak.recap.core.model.user.AccountInfo
import com.chalkak.recap.core.model.user.ConsentStatus
import com.chalkak.recap.core.model.user.DataSummary
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

@Singleton
class MockUserRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mockScreenshotDataResetter: MockScreenshotDataResetter,
    private val changeNotifier: RemoteCaptureChangeNotifier,
) : UserRepository {
    private val consentRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override suspend fun getAccountInfo(): Result<AccountInfo> =
        Result.failure(UnsupportedOperationException("Auth is remote-only"))

    override fun observeDataSummary(): Flow<Result<DataSummary>> =
        screenshotCardRepository.observeStoredCards().map { cards ->
            Result.success(DataSummary(capturedCount = cards.size.toLong()))
        }

    override suspend fun prefetchDataSummary(): Result<DataSummary> =
        observeDataSummary().first()

    override fun refreshDataSummary() = Unit

    override suspend fun getDataSummary(): Result<DataSummary> =
        prefetchDataSummary()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeConsentStatus(): Flow<Result<ConsentStatus>> =
        consentRefresh
            .onStart { emit(Unit) }
            .mapLatest {
                runCatching {
                    userPreferencesRepository.getAiDataTransferConsentStatus()
                }
            }

    override suspend fun prefetchConsentStatus(): Result<ConsentStatus> =
        observeConsentStatus().first()

    override fun refreshConsentStatus() {
        consentRefresh.tryEmit(Unit)
    }

    override suspend fun getConsentStatus(): Result<ConsentStatus> =
        prefetchConsentStatus()

    override suspend fun giveConsent(): Result<Unit> {
        val result = runCatching {
            userPreferencesRepository.setAiDataTransferConsent(
                consented = true,
                consentedAt = Instant.now().toString(),
            )
        }
        if (result.isSuccess) {
            refreshConsentStatus()
        }
        return result
    }

    override suspend fun withdrawConsent(): Result<Unit> {
        val result = runCatching {
            userPreferencesRepository.setAiDataTransferConsent(
                consented = false,
                consentedAt = null,
            )
        }
        if (result.isSuccess) {
            refreshConsentStatus()
        }
        return result
    }

    override suspend fun withdraw(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Auth is remote-only"))

    override suspend fun deleteAccountData(): Result<Unit> =
        runCatching {
            mockScreenshotDataResetter.reset()
            changeNotifier.notifyCaptureChanged()
        }
}
