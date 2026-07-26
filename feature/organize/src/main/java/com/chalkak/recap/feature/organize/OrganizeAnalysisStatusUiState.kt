package com.chalkak.recap.feature.organize

/**
 * 정리 진행/결과 오버레이에 노출할 UI 상태.
 * app 모듈의 분석 ViewModel 상태를 feature 계층 모델로 매핑해 사용한다.
 */
sealed interface OrganizeAnalysisStatusUiState {
    data object Hidden : OrganizeAnalysisStatusUiState

    data class Progress(
        val progress: Float,
    ) : OrganizeAnalysisStatusUiState

    data class Success(
        val successCount: Int,
    ) : OrganizeAnalysisStatusUiState

    data object Failed : OrganizeAnalysisStatusUiState

    data class PartialFailed(
        val successCount: Int,
    ) : OrganizeAnalysisStatusUiState
}
