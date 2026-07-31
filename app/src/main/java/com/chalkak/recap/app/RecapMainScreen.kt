package com.chalkak.recap.app

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.core.data.screenshot.permission.ImagePermissionRequestDestination
import com.chalkak.recap.core.data.screenshot.permission.currentImageAccessLevel
import com.chalkak.recap.core.data.screenshot.permission.imagePermissionRequestDestination
import com.chalkak.recap.core.data.screenshot.permission.openPhotoAccessPermission
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBar
import com.chalkak.recap.core.design.component.bottombar.RecapBottomBarDestination
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.ImageAccessLevel
import com.chalkak.recap.core.model.ScreenshotUploadCandidate
import com.chalkak.recap.feature.organize.OrganizeRoute
import dev.chrisbanes.haze.HazePositionStrategy
import dev.chrisbanes.haze.rememberHazeState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecapMainScreen(
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToRecentOrganizedScreenshots: () -> Unit = {},
    onOrganizeComplete: (List<ScreenshotUploadCandidate>) -> Unit = {},
    onNavigateToScreenshot: (Long) -> Unit = {},
    pendingHomeNavigationRequestId: Int? = null,
    onHomeNavigationComplete: (Int) -> Unit = {},
    pendingOpenOrganize: Boolean = false,
    onPendingOpenOrganizeConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(MainTabRoute.Home)
    val currentRoute = backStack.lastOrNull() as? MainTabRoute ?: MainTabRoute.Home
    val hazeState = rememberHazeState(positionStrategy = HazePositionStrategy.Screen)
    var openCollectionFavoritesOnNextEnter by remember { mutableStateOf(false) }
    var openCollectionTypeDetailOnNextEnter by remember { mutableStateOf<String?>(null) }
    var collectionPredictiveBackProgress by remember { mutableFloatStateOf(0f) }
    var showOrganize by rememberSaveable { mutableStateOf(false) }
    var showPhotoPermissionPopup by rememberSaveable { mutableStateOf(false) }
    var photoAccessLevel by remember {
        mutableStateOf(context.currentImageAccessLevel())
    }
    // 앱 설정 화면에서 돌아온 뒤에만 organize 오픈을 시도한다.
    // 시스템 권한 다이얼로그는 pause/resume을 유발하므로 런처 콜백으로만 처리한다.
    var awaitPermissionFromSettings by rememberSaveable { mutableStateOf(false) }

    fun refreshPhotoAccessLevel() {
        photoAccessLevel = context.currentImageAccessLevel()
    }

    fun attemptOpenOrganize() {
        refreshPhotoAccessLevel()
        if (photoAccessLevel == ImageAccessLevel.Denied) {
            showPhotoPermissionPopup = true
        } else {
            showOrganize = true
        }
    }

    fun finishPermissionRequest() {
        refreshPhotoAccessLevel()
        showPhotoPermissionPopup = false
        awaitPermissionFromSettings = false
        if (photoAccessLevel != ImageAccessLevel.Denied) {
            showOrganize = true
        }
    }

    LifecycleResumeEffect(Unit) {
        refreshPhotoAccessLevel()
        if (awaitPermissionFromSettings) {
            finishPermissionRequest()
        }
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        finishPermissionRequest()
    }

    LaunchedEffect(pendingOpenOrganize) {
        if (pendingOpenOrganize) {
            onPendingOpenOrganizeConsumed()
            attemptOpenOrganize()
        }
    }

    fun navigateTo(route: MainTabRoute) {
        if (backStack.lastOrNull() == route) return
        when (route) {
            MainTabRoute.Home -> {
                backStack.clear()
                backStack.add(MainTabRoute.Home)
            }

            MainTabRoute.Collection -> {
                // Keep Home under Collection so system back returns to Home.
                while (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
                if (backStack.lastOrNull() != MainTabRoute.Home) {
                    backStack.clear()
                    backStack.add(MainTabRoute.Home)
                }
                backStack.add(MainTabRoute.Collection)
            }
        }
    }

    fun navigateToCollectionFavorites() {
        openCollectionTypeDetailOnNextEnter = null
        openCollectionFavoritesOnNextEnter = true
        navigateTo(MainTabRoute.Collection)
    }

    fun navigateToCollectionTypeDetail(contentTypeName: String) {
        openCollectionFavoritesOnNextEnter = false
        openCollectionTypeDetailOnNextEnter = contentTypeName
        navigateTo(MainTabRoute.Collection)
    }

    LaunchedEffect(pendingHomeNavigationRequestId) {
        pendingHomeNavigationRequestId?.let { requestId ->
            navigateTo(MainTabRoute.Home)
            onHomeNavigationComplete(requestId)
        }
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute != MainTabRoute.Collection) {
            collectionPredictiveBackProgress = 0f
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                RecapBottomBar(
                    hazeState = hazeState,
                    currentDestination = currentRoute.toBottomBarDestination(),
                    predictiveBackProgress = collectionPredictiveBackProgress,
                    onDestinationClick = { destination ->
                        navigateTo(destination.toMainTabRoute())
                    },
                    onOrganizeClick = {
                        attemptOpenOrganize()
                    },
                )
            },
        ) { _ ->
            RecapMainTabNavHost(
                hazeState = hazeState,
                backStack = backStack,
                onNavigateToDeveloper = onNavigateToDeveloper,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToRecentOrganizedScreenshots = onNavigateToRecentOrganizedScreenshots,
                onNavigateToOrganize = {
                    attemptOpenOrganize()
                },
                onNavigateToCollectionFavorites = ::navigateToCollectionFavorites,
                onNavigateToCollectionTypeDetail = ::navigateToCollectionTypeDetail,
                onNavigateToScreenshot = onNavigateToScreenshot,
                openCollectionFavoritesOnEnter = openCollectionFavoritesOnNextEnter,
                onOpenCollectionFavoritesOnEnterConsumed = {
                    openCollectionFavoritesOnNextEnter = false
                },
                openCollectionTypeDetailOnEnter = openCollectionTypeDetailOnNextEnter,
                onOpenCollectionTypeDetailOnEnterConsumed = {
                    openCollectionTypeDetailOnNextEnter = null
                },
                showDeveloperLogoShortcut = BuildConfig.DEBUG,
                onCollectionPredictiveBackProgress = { progress ->
                    collectionPredictiveBackProgress = progress
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (showOrganize) {
            key(NORMAL_ORGANIZE_SESSION_KEY) {
                OrganizeRoute(
                    onNavigateBack = {
                        showOrganize = false
                    },
                    onOrganizeComplete = { selectedScreenshots ->
                        showOrganize = false
                        onOrganizeComplete(selectedScreenshots)
                    },
                )
            }
        }

        if (showPhotoPermissionPopup) {
            RecapPopup(
                title = stringResource(R.string.photo_access_permission_title),
                description = stringResource(R.string.photo_access_permission_description),
                confirmButtonText = stringResource(
                    R.string.photo_access_permission_request_permission,
                ),
                cancelButtonText = stringResource(R.string.photo_access_permission_later_button),
                onConfirmClick = {
                    if (
                        context.imagePermissionRequestDestination() ==
                        ImagePermissionRequestDestination.ApplicationSettings
                    ) {
                        awaitPermissionFromSettings = true
                    }
                    openPhotoAccessPermission(
                        context = context,
                        photoAccessLevel = photoAccessLevel,
                        onRequestPermissions = { permissions ->
                            permissionLauncher.launch(permissions)
                        },
                    )
                },
                onCancelClick = {
                    showPhotoPermissionPopup = false
                    awaitPermissionFromSettings = false
                },
                onDismissRequest = {
                    showPhotoPermissionPopup = false
                    awaitPermissionFromSettings = false
                },
                confirmButtonColor = RecapBlue300,
                confirmButtonContentColor = White,
            )
        }
    }
}

private fun MainTabRoute.toBottomBarDestination(): RecapBottomBarDestination = when (this) {
    MainTabRoute.Home -> RecapBottomBarDestination.Home
    MainTabRoute.Collection -> RecapBottomBarDestination.Collection
}

private fun RecapBottomBarDestination.toMainTabRoute(): MainTabRoute = when (this) {
    RecapBottomBarDestination.Home -> MainTabRoute.Home
    RecapBottomBarDestination.Collection -> MainTabRoute.Collection
}

private const val NORMAL_ORGANIZE_SESSION_KEY = "normal-organize"
