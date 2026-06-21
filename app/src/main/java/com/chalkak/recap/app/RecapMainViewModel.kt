package com.chalkak.recap.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecapMainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RecapMainUiState())
    val uiState: StateFlow<RecapMainUiState> = _uiState.asStateFlow()

    fun onRouteSelected(route: RecapRoute) {
        _uiState.update { current ->
            current.copy(selectedRoute = route)
        }
    }

    fun onDestinationChanged(route: RecapRoute) {
        _uiState.update { current ->
            if (current.selectedRoute == route) {
                current
            } else {
                current.copy(selectedRoute = route)
            }
        }
    }
}
