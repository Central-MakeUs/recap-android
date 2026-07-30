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
import kotlinx.coroutines.flow.first

@Singleton
class MockUserRepository @Inject constructor(
    private val screenshotCardRepository: ScreenshotCardRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mockScreenshotDataResetter: MockScreenshotDataResetter,
    private val changeNotifier: RemoteCaptureChangeNotifier,
) : UserRepository {
    override suspend fun getAccountInfo(): Result<AccountInfo> =
        Result.failure(UnsupportedOperationException("Auth is remote-only"))

    override suspend fun getDataSummary(): Result<DataSummary> =
        runCatching {
            val count = screenshotCardRepository.observeStoredCards().first().size.toLong()
            DataSummary(capturedCount = count)
        }

    override suspend fun getConsentStatus(): Result<ConsentStatus> =
        runCatching {
            userPreferencesRepository.getAiDataTransferConsentStatus()
        }

    override suspend fun giveConsent(): Result<Unit> =
        runCatching {
            userPreferencesRepository.setAiDataTransferConsent(
                consented = true,
                consentedAt = Instant.now().toString(),
            )
        }

    override suspend fun withdrawConsent(): Result<Unit> =
        runCatching {
            userPreferencesRepository.setAiDataTransferConsent(
                consented = false,
                consentedAt = null,
            )
        }

    override suspend fun withdraw(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Auth is remote-only"))

    override suspend fun deleteAccountData(): Result<Unit> =
        runCatching {
            mockScreenshotDataResetter.reset()
            changeNotifier.notifyCaptureChanged()
        }
}
