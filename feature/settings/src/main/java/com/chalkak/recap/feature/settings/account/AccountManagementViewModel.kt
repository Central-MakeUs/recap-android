package com.chalkak.recap.feature.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.LocalAppDataResetter
import com.chalkak.recap.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountManagementViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val localAppDataResetter: LocalAppDataResetter,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountManagementUiState())
    val uiState: StateFlow<AccountManagementUiState> = _uiState.asStateFlow()

    fun loadAccountInfo() {
        viewModelScope.launch {
            val loaded = authRepository.getKakaoUserProfile().getOrNull()
            _uiState.update {
                it.copy(
                    joinedDate = loaded?.connectedAt?.let(::formatJoinedDate).orEmpty(),
                )
            }
        }
    }

    fun onAction(action: AccountManagementAction) {
        when (action) {
            AccountManagementAction.NavigateBack -> Unit
            AccountManagementAction.LogoutClick -> {
                _uiState.update { it.copy(dialog = AccountManagementDialog.Logout) }
            }
            AccountManagementAction.WithdrawClick -> {
                _uiState.update { it.copy(dialog = AccountManagementDialog.Withdraw) }
            }
            AccountManagementAction.DismissDialog -> {
                _uiState.update { it.copy(dialog = AccountManagementDialog.None) }
            }
            AccountManagementAction.ConfirmLogout,
            AccountManagementAction.ConfirmWithdraw,
            -> {
                _uiState.update { it.copy(dialog = AccountManagementDialog.None) }
                logoutAndResetLocalData()
            }
        }
    }

    private fun logoutAndResetLocalData() {
        viewModelScope.launch {
            // 서버 실패여도 AuthRepository가 로컬 토큰을 clear한다.
            authRepository.logout()
            localAppDataResetter.resetDatabaseAndOnboarding()
        }
    }
}
