package com.chalkak.recap.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.user.UserRepository
import com.chalkak.recap.core.model.user.Usage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecapMainUiState(val exhaustedUsage: Usage? = null)

sealed interface RecapMainEvent {
    data object OpenOrganize : RecapMainEvent
}

@HiltViewModel
class RecapMainViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecapMainUiState())
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<RecapMainEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    private var entryJob: Job? = null

    fun requestOrganize() {
        if (entryJob?.isActive == true) return
        entryJob = viewModelScope.launch {
            val usage = userRepository.getCachedUsage()
            if (usage != null && usage.remaining <= 0) {
                _uiState.value = RecapMainUiState(exhaustedUsage = usage)
            } else {
                _uiState.value = RecapMainUiState()
                _events.emit(RecapMainEvent.OpenOrganize)
            }
        }
    }

    fun dismissMonthlyLimit() {
        _uiState.value = RecapMainUiState()
    }
}
