package com.chalkak.recap.core.data.entity

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.entityextraction.EntityExtractionRemoteModel
import com.google.mlkit.nl.entityextraction.EntityExtractorOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class EntityExtractionModelDownloader @Inject constructor() {
    private val remoteModelManager = RemoteModelManager.getInstance()
    private val koreanModel = EntityExtractionRemoteModel
        .Builder(EntityExtractorOptions.KOREAN)
        .build()
    private val downloadMutex = Mutex()
    private val _downloadState = MutableStateFlow<EntityExtractionModelDownloadState>(
        EntityExtractionModelDownloadState.Idle,
    )

    val downloadState: StateFlow<EntityExtractionModelDownloadState> = _downloadState.asStateFlow()

    suspend fun refreshDownloadState() {
        _downloadState.value = if (isKoreanModelDownloaded()) {
            EntityExtractionModelDownloadState.Downloaded
        } else {
            EntityExtractionModelDownloadState.NotDownloaded
        }
    }

    suspend fun downloadKoreanModelIfNeeded() {
        downloadMutex.withLock {
            if (isKoreanModelDownloaded()) {
                _downloadState.value = EntityExtractionModelDownloadState.Downloaded
                return
            }

            _downloadState.value = EntityExtractionModelDownloadState.Downloading

            runCatching {
                remoteModelManager
                    .download(koreanModel, DownloadConditions.Builder().build())
                    .await()
            }.onSuccess {
                _downloadState.value = EntityExtractionModelDownloadState.Downloaded
            }.onFailure {
                _downloadState.value = EntityExtractionModelDownloadState.Failed
            }
        }
    }

    private suspend fun isKoreanModelDownloaded(): Boolean {
        return remoteModelManager
            .getDownloadedModels(EntityExtractionRemoteModel::class.java)
            .await()
            .contains(koreanModel)
    }
}

sealed interface EntityExtractionModelDownloadState {
    data object Idle : EntityExtractionModelDownloadState
    data object NotDownloaded : EntityExtractionModelDownloadState
    data object Downloading : EntityExtractionModelDownloadState
    data object Downloaded : EntityExtractionModelDownloadState
    data object Failed : EntityExtractionModelDownloadState
}

private suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { throwable ->
            continuation.resumeWithException(throwable)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}
