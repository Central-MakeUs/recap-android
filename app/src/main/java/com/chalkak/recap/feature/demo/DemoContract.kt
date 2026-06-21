package com.chalkak.recap.feature.demo

import android.net.Uri

data class DemoUiState(
    val title: String = "Demo",
    val description: String = "이미지 권한 정책과 가져오기 플로우를 확인합니다.",
    val imagePermissionLevel: ImagePermissionLevel = ImagePermissionLevel.Denied,
    val recentScreenshotUris: List<Uri> = emptyList(),
)

sealed interface DemoAction {
    data object RequestImagePermission : DemoAction
    data object RefreshImagePermission : DemoAction
}

enum class ImagePermissionLevel(
    val label: String,
    val description: String,
) {
    Full(
        label = "전체 이미지",
        description = "MediaStore에서 이미지 목록을 조회할 수 있습니다.",
    ),
    Selected(
        label = "선택한 이미지만",
        description = "사용자가 허용한 일부 이미지만 앱에서 볼 수 있습니다.",
    ),
    Denied(
        label = "권한 없음",
        description = "이미지 자동 조회는 불가하며, 선택 플로우가 필요합니다.",
    ),
}
