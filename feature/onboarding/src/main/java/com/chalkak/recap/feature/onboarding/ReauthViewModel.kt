package com.chalkak.recap.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.auth.AuthException
import com.chalkak.recap.core.data.auth.AuthRepository
import com.chalkak.recap.core.model.auth.AuthError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReauthUiState(
    val isLoading: Boolean = false,
)

sealed interface ReauthEvent {
    data class ShowLoginError(val isCancelled: Boolean) : ReauthEvent
}

/**
 * 온보딩을 마친 사용자가 세션을 잃었을 때의 재로그인.
 *
 * 온보딩 단계 머신과 권한 요청을 거치지 않으며, 로그인 성공 시 세션 저장만으로
 * 루트 라우팅이 Main으로 복귀한다.
 */
@HiltViewModel
class ReauthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReauthUiState())
    val uiState: StateFlow<ReauthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReauthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ReauthEvent> = _events.asSharedFlow()

    fun loginWithKakao(context: Context) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoading = true) }

            authRepository.signInWithKakao(context).fold(
                onSuccess = {
                    _uiState.update { current -> current.copy(isLoading = false) }
                },
                onFailure = { error ->
                    val authError = (error as? AuthException)?.authError ?: AuthError.Unknown
                    _uiState.update { current -> current.copy(isLoading = false) }
                    _events.emit(
                        ReauthEvent.ShowLoginError(
                            isCancelled = authError == AuthError.Cancelled,
                        ),
                    )
                },
            )
        }
    }
}
