package com.chalkak.recap.core.data

import com.chalkak.recap.core.data.account.AccountOwnerStore
import com.chalkak.recap.core.data.capture.RemoteCaptureThumbnailCache
import com.chalkak.recap.core.data.network.SessionTokenStore
import com.chalkak.recap.core.data.search.RecentSearchStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LocalAppDataResetterTest {
    private val recapDatabase = mockk<RecapDatabase>(relaxed = true)
    private val thumbnailCache = mockk<RemoteCaptureThumbnailCache>(relaxed = true)
    private val userPreferencesRepository = mockk<UserPreferencesRepository>(relaxed = true)
    private val sessionTokenStore = mockk<SessionTokenStore>(relaxed = true)
    private val recentSearchStore = mockk<RecentSearchStore>(relaxed = true)
    private val accountOwnerStore = mockk<AccountOwnerStore>(relaxed = true)

    private lateinit var resetter: LocalAppDataResetter

    @BeforeEach
    fun setUp() {
        every { thumbnailCache.clearAll() } returns true
        coEvery { accountOwnerStore.clear() } just runs
        coEvery { accountOwnerStore.setHash(any()) } just runs
        coEvery { recentSearchStore.clearAll() } just runs
        coEvery { sessionTokenStore.clear() } just runs
        coEvery { userPreferencesRepository.setOnboardingCompleted(any()) } just runs
        coEvery { userPreferencesRepository.clearAccountScopedPreferences() } just runs
        every { recapDatabase.clearAllTables() } just runs

        resetter = LocalAppDataResetter(
            recapDatabase = recapDatabase,
            thumbnailCache = thumbnailCache,
            userPreferencesRepository = userPreferencesRepository,
            sessionTokenStore = sessionTokenStore,
            recentSearchStore = recentSearchStore,
            accountOwnerStore = accountOwnerStore,
        )
    }

    @Test
    fun `wipeAndRebindOwner clears account data without touching onboarding or session`() =
        runTest {
            resetter.wipeAndRebindOwner(OWNER_HASH)

            verify(exactly = 1) { recapDatabase.clearAllTables() }
            verify(exactly = 1) { thumbnailCache.clearAll() }
            coVerify(exactly = 1) { recentSearchStore.clearAll() }
            coVerify(exactly = 1) { userPreferencesRepository.clearAccountScopedPreferences() }
            coVerify(exactly = 1) { accountOwnerStore.setHash(OWNER_HASH) }
            coVerify(exactly = 0) { userPreferencesRepository.setOnboardingCompleted(any()) }
            coVerify(exactly = 0) { sessionTokenStore.clear() }
            coVerify(exactly = 0) { accountOwnerStore.clear() }
        }

    @Test
    fun `wipeAndRebindOwner keeps previous owner hash when image clear fails`() = runTest {
        every { thumbnailCache.clearAll() } returns false

        assertThrows<IOException> {
            resetter.wipeAndRebindOwner(OWNER_HASH)
        }

        coVerify(exactly = 0) { accountOwnerStore.setHash(any()) }
    }

    @Test
    fun `resetDatabaseAndOnboarding clears owner hash account prefs and onboarding`() = runTest {
        resetter.resetDatabaseAndOnboarding()

        coVerify(exactly = 1) { userPreferencesRepository.setOnboardingCompleted(false) }
        verify(exactly = 1) { recapDatabase.clearAllTables() }
        verify(exactly = 1) { thumbnailCache.clearAll() }
        coVerify(exactly = 1) { sessionTokenStore.clear() }
        coVerify(exactly = 1) { recentSearchStore.clearAll() }
        coVerify(exactly = 1) { userPreferencesRepository.clearAccountScopedPreferences() }
        coVerify(exactly = 1) { accountOwnerStore.clear() }
    }

    @Test
    fun `resetDatabaseAndOnboarding clears session even when image clear fails`() = runTest {
        every { thumbnailCache.clearAll() } returns false

        resetter.resetDatabaseAndOnboarding()

        coVerify(exactly = 1) { sessionTokenStore.clear() }
        coVerify(exactly = 1) { accountOwnerStore.clear() }
    }

    private companion object {
        const val OWNER_HASH = "owner-hash-a"
    }
}
