package com.chalkak.recap.feature.developer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.entity.EntityExtractionModelDownloadState
import com.chalkak.recap.core.data.entity.EntityExtractionModelDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DeveloperOptionsViewModel @Inject constructor(
    private val entityExtractionModelDownloader: EntityExtractionModelDownloader,
) : ViewModel() {
    val modelDownloadState: StateFlow<EntityExtractionModelDownloadState> =
        entityExtractionModelDownloader.downloadState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EntityExtractionModelDownloadState.Idle,
        )

    init {
        refreshModelDownloadState()
    }

    fun downloadEntityExtractionModel() {
        viewModelScope.launch {
            entityExtractionModelDownloader.downloadKoreanModelIfNeeded()
        }
    }

    private fun refreshModelDownloadState() {
        viewModelScope.launch {
            entityExtractionModelDownloader.refreshDownloadState()
        }
    }
}
