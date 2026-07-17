package com.chalkak.recap.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chalkak.recap.core.data.LocalAppDataResetter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountManagementViewModel @Inject constructor(
    private val localAppDataResetter: LocalAppDataResetter,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AccountManagementUiState(
            // 계정 프로필 API 연동 전 임시 표시값
            accountEmail = PREVIEW_ACCOUNT_EMAIL,
            joinedDate = PREVIEW_JOINED_DATE,
        ),
    )
    val uiState: StateFlow<AccountManagementUiState> = _uiState.asStateFlow()

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
                resetLocalAccountData()
            }
        }
    }

    private fun resetLocalAccountData() {
        viewModelScope.launch {
            localAppDataResetter.resetDatabaseAndOnboarding()
        }
    }

    companion object {
        // string resource 값은 ViewModel에서 읽지 않으므로 화면 프리뷰와 동일한 임시값을 둔다.
        const val PREVIEW_ACCOUNT_EMAIL = "Recap@kakao.com"
        const val PREVIEW_JOINED_DATE = "2026.6.12"
    }
}
