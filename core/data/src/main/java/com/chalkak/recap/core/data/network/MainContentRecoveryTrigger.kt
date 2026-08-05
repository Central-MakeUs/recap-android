package com.chalkak.recap.core.data.network

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Main Home/Collection/Search가 로드 에러일 때 자동 refresh를 유도하는 복구 시그널.
 *
 * - 앱이 foreground로 돌아오고([ProcessLifecycleOwner] onStart) 인터넷이 validated일 때
 * - validated 인터넷이 `false → true`로 바뀔 때
 *
 * 시그널 1회당 ViewModel은 Error일 때만 refresh를 1회 시도한다.
 */
@Singleton
class MainContentRecoveryTrigger internal constructor(
    private val networkConnectivityMonitor: NetworkConnectivityMonitor,
    processLifecycle: Lifecycle,
    private val scope: CoroutineScope,
) {
    private val _recoveries = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    val recoveries: SharedFlow<Unit> = _recoveries.asSharedFlow()

    @Inject
    constructor(
        networkConnectivityMonitor: NetworkConnectivityMonitor,
    ) : this(
        networkConnectivityMonitor = networkConnectivityMonitor,
        processLifecycle = ProcessLifecycleOwner.get().lifecycle,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    )

    init {
        processLifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (networkConnectivityMonitor.isInternetValidated()) {
                        scope.launch {
                            _recoveries.emit(Unit)
                        }
                    }
                }
            },
        )
        scope.launch {
            var previous: Boolean? = null
            networkConnectivityMonitor.validatedInternet.collect { current ->
                val was = previous
                previous = current
                if (was == false && current) {
                    _recoveries.emit(Unit)
                }
            }
        }
    }
}
