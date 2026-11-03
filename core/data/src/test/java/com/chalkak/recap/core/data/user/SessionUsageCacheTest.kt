package com.chalkak.recap.core.data.user

import app.cash.turbine.test
import com.chalkak.recap.core.model.user.Usage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class SessionUsageCacheTest {
    private val sessions = MutableStateFlow<String?>("account-a")
    private val usage = Usage(4, 20, 16, "2026-12-01T00:00:00Z")
    private val clock = MutableClock(Instant.parse("2026-11-03T00:00:00Z"))

    @Test
    fun `peek reads valid cache without fetching and ignores expired or signed out cache`() =
        runTest {
            var calls = 0
            val cache = SessionUsageCache(backgroundScope, sessions, {
                calls++
                Result.success(usage)
            }, clock)
            assertNull(cache.peek())
            assertEquals(0, calls)
            cache.get()
            assertEquals(usage, cache.peek())
            assertEquals(1, calls)
            clock.now = Instant.parse("2026-12-01T00:00:00Z")
            assertNull(cache.peek())
            sessions.value = null
            assertNull(cache.peek())
            assertEquals(1, calls)
        }

    @Test
    fun `cache survives gaps between consumers and forced refresh updates observers`() = runTest {
        var calls = 0
        val cache = SessionUsageCache(backgroundScope, sessions, {
            calls++
            Result.success(usage.copy(usedCount = calls, remaining = 20 - calls))
        }, clock)

        assertEquals(19, cache.get().getOrThrow().remaining)
        assertEquals(19, cache.get().getOrThrow().remaining)
        assertEquals(1, calls)
        cache.observe().test {
            assertEquals(19, awaitItem().getOrThrow().remaining)
            cache.get(forceRefresh = true)
            assertEquals(18, awaitItem().getOrThrow().remaining)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(18, cache.get().getOrThrow().remaining)
        assertEquals(2, calls)
    }

    @Test
    fun `concurrent startup and external requests share network work`() = runTest {
        val response = CompletableDeferred<Result<Usage>>()
        var calls = 0
        val cache = SessionUsageCache(backgroundScope, sessions, { calls++; response.await() }, clock)
        val startup = async { cache.get() }
        runCurrent()
        val external = async { cache.get(forceRefresh = true) }
        runCurrent()
        response.complete(Result.success(usage))

        assertEquals(usage, startup.await().getOrThrow())
        assertEquals(usage, external.await().getOrThrow())
        assertEquals(1, calls)
    }

    @Test
    fun `failed first request retries and failed refresh preserves valid cache`() = runTest {
        var calls = 0
        val cache = SessionUsageCache(backgroundScope, sessions, {
            calls++
            if (calls == 2) Result.success(usage) else Result.failure(IllegalStateException())
        }, clock)

        assertTrue(cache.get().isFailure)
        assertEquals(usage, cache.get().getOrThrow())
        assertEquals(usage, cache.get(forceRefresh = true).getOrThrow())
        assertEquals(usage, cache.get().getOrThrow())
        assertEquals(3, calls)
    }

    @Test
    fun `logout clears cache and next account fetches its own usage`() = runTest {
        var calls = 0
        val cache = SessionUsageCache(backgroundScope, sessions, {
            calls++
            Result.success(usage.copy(remaining = calls))
        }, clock)
        assertEquals(1, cache.get().getOrThrow().remaining)
        sessions.value = null
        runCurrent()
        assertTrue(cache.get().isFailure)
        sessions.value = "account-b"
        assertEquals(2, cache.get().getOrThrow().remaining)
    }

    @Test
    fun `late response from logged out session cannot overwrite new account`() = runTest {
        val oldResponse = CompletableDeferred<Result<Usage>>()
        var calls = 0
        val cache = SessionUsageCache(backgroundScope, sessions, {
            calls++
            if (calls == 1) oldResponse.await() else Result.success(usage.copy(remaining = 5))
        }, clock)
        val oldRequest = async { cache.get() }
        runCurrent()
        sessions.value = null
        runCurrent()
        sessions.value = "account-b"
        assertEquals(5, cache.get().getOrThrow().remaining)
        oldResponse.complete(Result.success(usage))

        assertTrue(oldRequest.await().isFailure)
        assertEquals(5, cache.get().getOrThrow().remaining)
    }

    @Test
    fun `monthly reset invalidates cached usage`() = runTest {
        var calls = 0
        val cache = SessionUsageCache(backgroundScope, sessions, {
            calls++
            Result.success(usage)
        }, clock)
        cache.get()
        clock.now = Instant.parse("2026-12-01T00:00:00Z")
        cache.get()
        assertEquals(2, calls)
    }

    private class MutableClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = now
    }
}
