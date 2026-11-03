package com.chalkak.recap.core.data.user

import com.chalkak.recap.core.model.user.Usage
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SessionUsageCache(
    private val scope: CoroutineScope,
    private val sessions: Flow<String?>,
    private val fetch: suspend () -> Result<Usage>,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val mutex = Mutex()
    private val state = MutableStateFlow<Entry?>(null)
    private var generation = 0L
    private var session: String? = null
    private var request: Deferred<Result<Usage>>? = null
    private var cached: Usage? = null
    private var expiresAt = Instant.MIN

    init {
        scope.launch {
            sessions.distinctUntilChanged().collect { key ->
                mutex.withLock { changeSession(key) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<Result<Usage>> = sessions.flatMapLatest { key ->
        flow {
            emit(get())
            emitAll(state.filterNotNull().map { entry ->
                entry.takeIf { it.session == key }?.result
            }.filterNotNull())
        }.distinctUntilChanged()
    }

    suspend fun get(forceRefresh: Boolean = false): Result<Usage> {
        val key = sessions.first()
        val (requestGeneration, pending) = mutex.withLock {
            changeSession(key)
            if (key == null) return Result.failure(IllegalStateException("Usage session unavailable"))
            request?.takeIf { it.isActive }?.let { return@withLock generation to it }
            val valid = cached?.takeIf { clock.instant().isBefore(expiresAt) }
            if (!forceRefresh && valid != null) return Result.success(valid)
            val currentGeneration = generation
            scope.async(start = CoroutineStart.LAZY) {
                val result = fetch()
                mutex.withLock {
                    if (session == key && generation == currentGeneration) {
                        val usage = result.getOrNull()
                        if (usage != null) {
                            cached = usage
                            val now = clock.instant()
                            val nextMonth = now.atZone(ZoneOffset.UTC).toLocalDate()
                                .withDayOfMonth(1).plusMonths(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                            expiresAt = runCatching { Instant.parse(usage.resetAt) }.getOrNull()
                                ?.takeIf { it.isAfter(now) } ?: nextMonth
                        }
                        val fallback = cached?.takeIf { clock.instant().isBefore(expiresAt) }
                        state.value = Entry(key, fallback?.let { Result.success(it) } ?: result)
                    }
                }
                result
            }.also {
                request = it
                it.start()
            }.let { generation to it }
        }
        val result = pending.await()
        return mutex.withLock {
            if (session != key || generation != requestGeneration) {
                Result.failure(IllegalStateException("Usage session changed"))
            } else {
                cached?.takeIf { clock.instant().isBefore(expiresAt) }?.let { Result.success(it) } ?: result
            }
        }
    }

    private fun changeSession(key: String?) {
        if (session == key) return
        generation += 1
        session = key
        cached = null
        expiresAt = Instant.MIN
        request = null
        state.value = if (key == null) {
            Entry(null, Result.failure(IllegalStateException("Usage session unavailable")))
        } else null
    }

    private data class Entry(val session: String?, val result: Result<Usage>)
}
