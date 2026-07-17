package com.chalkak.recap.core.data.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.UUID

class DeviceIdProviderTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var provider: DeviceIdProvider

    @BeforeEach
    fun setUp() {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempDir, "user_preferences.preferences_pb") },
        )
        provider = DeviceIdProvider(dataStore)
    }

    @Test
    fun `getOrCreate creates a UUID on first call`() = runTest {
        val deviceId = provider.getOrCreate()

        assertTrue(runCatching { UUID.fromString(deviceId) }.isSuccess)
    }

    @Test
    fun `getOrCreate returns the same value on subsequent calls`() = runTest {
        val first = provider.getOrCreate()
        val second = provider.getOrCreate()

        assertEquals(first, second)
    }

    @Test
    fun `getOrCreate creates distinct ids for separate providers`() = runTest {
        val otherDataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempDir, "other_preferences.preferences_pb") },
        )
        val otherProvider = DeviceIdProvider(otherDataStore)

        val first = provider.getOrCreate()
        val second = otherProvider.getOrCreate()

        assertNotEquals(first, second)
    }

    @Test
    fun `concurrent getOrCreate returns a single shared id`() = runTest {
        val ids = (1..20)
            .map { async { provider.getOrCreate() } }
            .awaitAll()

        assertEquals(1, ids.distinct().size)
        assertTrue(runCatching { UUID.fromString(ids.first()) }.isSuccess)
    }
}
