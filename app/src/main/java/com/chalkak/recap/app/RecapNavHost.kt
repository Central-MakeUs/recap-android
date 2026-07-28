package com.chalkak.recap.app

import android.content.Intent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.chalkak.recap.core.design.animation.RecapNavDisplay
import com.chalkak.recap.core.design.animation.RecapNavigationMotion
import com.chalkak.recap.feature.collection.CollectionRoute
import com.chalkak.recap.feature.home.HomeRoute
import com.chalkak.recap.feature.home.recent.RecentOrganizedScreenshotsRoute
import com.chalkak.recap.feature.home.search.SearchRoute
import com.chalkak.recap.feature.organize.OrganizeAnalysisStatusRoute
import com.chalkak.recap.feature.organize.OrganizeAnalysisStatusUiState
import com.chalkak.recap.feature.settings.account.AccountManagementRoute
import com.chalkak.recap.feature.settings.data.DataManagementRoute
import com.chalkak.recap.feature.settings.notification.NotificationSettingsRoute
import com.chalkak.recap.feature.settings.SettingsAction
import com.chalkak.recap.feature.settings.SettingsRoute
import com.chalkak.recap.feature.onboarding.screen.OnboardingAddToFavoriteGuideScreen
import com.chalkak.recap.feature.settings.guide.PrivacyGuideScreen
import com.chalkak.recap.feature.settings.guide.UsageGuideScreen
import com.chalkak.recap.feature.screenshot.ScreenshotRoute
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.map

private const val MainTabSlideDurationMillis = 300
private const val MainTabFadeDurationMillis = 250
private const val MainTabSlideFraction = 6

@Composable
fun RecapNavHost(
    modifier: Modifier = Modifier,
    onNavigateToDeveloper: () -> Unit,
    pendingOpenOrganize: Boolean = false,
    onPendingOpenOrganizeConsumed: () -> Unit = {},
    analysisProgressViewModel: ScreenshotAnalysisProgressViewModel,
    pendingHomeNavigationRequestId: Int?,
    onRequestNavigateHome: () -> Unit,
    onHomeNavigationComplete: (Int) -> Unit,
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(AppRoute.MainTabs)
    var requestOpenOrganize by remember { mutableStateOf(false) }
    val analysisStatusFlow = remember(analysisProgressViewModel) {
        analysisProgressViewModel.uiState.map { state ->
            state.toOrganizeAnalysisStatusUiState()
        }
    }
    val initialAnalysisStatus = remember(analysisProgressViewModel) {
        analysisProgressViewModel.uiState.value.toOrganizeAnalysisStatusUiState()
    }
    val analysisStatus by analysisStatusFlow.collectAsStateWithLifecycle(
        initialValue = initialAnalysisStatus,
    )
    var renderedAnalysisStatus by remember {
        mutableStateOf(
            retainLastVisibleAnalysisStatus(
                previous = null,
                current = initialAnalysisStatus,
            ),
        )
    }

    fun exitOrganizeAnalysisStatus() {
        val wasRunning = analysisProgressViewModel.uiState.value.isRunning
        if (backStack.lastOrNull() == AppRoute.OrganizeAnalysisStatus) {
            backStack.removeLastOrNull()
        }
        if (wasRunning) {
            analysisProgressViewModel.cancelAnalysis()
        } else {
            analysisProgressViewModel.dismissResult()
        }
    }

    fun openOrganizeAnalysisStatusIfNeeded() {
        if (analysisProgressViewModel.uiState.value.isStatusVisible &&
            backStack.none { it == AppRoute.OrganizeAnalysisStatus }
        ) {
            backStack.add(AppRoute.OrganizeAnalysisStatus)
        }
    }

    fun isOrganizeAnalysisStatusTargetStack(showStatus: Boolean): Boolean {
        if (backStack.firstOrNull() != AppRoute.MainTabs) return false
        return if (showStatus) {
            backStack.size == 2 && backStack.lastOrNull() == AppRoute.OrganizeAnalysisStatus
        } else {
            backStack.size == 1
        }
    }

    LaunchedEffect(pendingHomeNavigationRequestId) {
        if (pendingHomeNavigationRequestId != null) {
            val showStatus = analysisProgressViewModel.uiState.value.isStatusVisible
            if (!isOrganizeAnalysisStatusTargetStack(showStatus)) {
                while (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
                if (backStack.lastOrNull() != AppRoute.MainTabs) {
                    backStack.clear()
                    backStack.add(AppRoute.MainTabs)
                }
                if (showStatus) {
                    openOrganizeAnalysisStatusIfNeeded()
                }
            }
        }
    }

    LaunchedEffect(analysisStatus) {
        renderedAnalysisStatus = retainLastVisibleAnalysisStatus(
            previous = renderedAnalysisStatus,
            current = analysisStatus,
        )
        if (analysisStatus !is OrganizeAnalysisStatusUiState.Hidden) {
            openOrganizeAnalysisStatusIfNeeded()
        }
    }

    RecapNavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.lastOrNull() == AppRoute.OrganizeAnalysisStatus) {
                exitOrganizeAnalysisStatus()
            } else {
                backStack.removeLastOrNull()
            }
        },
        modifier = modifier,
        transitionSpec = { RecapNavigationMotion.forward() },
        popTransitionSpec = { RecapNavigationMotion.pop() },
        entryProvider = { route ->
            when (route) {
                AppRoute.MainTabs -> NavEntry(route) {
                    val isMainTabsOnTop = backStack.lastOrNull() == AppRoute.MainTabs
                    val mainTabsDispatcherOwner = rememberNavigationEventDispatcherOwner(
                        enabled = isMainTabsOnTop,
                    )
                    CompositionLocalProvider(
                        LocalNavigationEventDispatcherOwner provides mainTabsDispatcherOwner,
                    ) {
                        RecapMainScreen(
                            onNavigateToDeveloper = onNavigateToDeveloper,
                            onNavigateToSettings = { backStack.add(AppRoute.Settings) },
                            onNavigateToSearch = { backStack.add(AppRoute.Search) },
                            onNavigateToRecentOrganizedScreenshots = {
                                backStack.add(AppRoute.RecentOrganizedScreenshots)
                            },
                            onOrganizeComplete = { selectedScreenshots ->
                                analysisProgressViewModel.startAnalysis(selectedScreenshots)
                                onRequestNavigateHome()
                            },
                            onNavigateToScreenshot = { captureId ->
                                if (captureId > 0) {
                                    backStack.add(AppRoute.Screenshot(captureId))
                                }
                            },
                            pendingHomeNavigationRequestId =
                                pendingHomeNavigationRequestId.takeIf {
                                    backStack.lastOrNull() == AppRoute.MainTabs
                                },
                            onHomeNavigationComplete = onHomeNavigationComplete,
                            pendingOpenOrganize = pendingOpenOrganize || requestOpenOrganize,
                            onPendingOpenOrganizeConsumed = {
                                if (requestOpenOrganize) {
                                    requestOpenOrganize = false
                                } else {
                                    onPendingOpenOrganizeConsumed()
                                }
                            },
                        )
                    }
                }

                AppRoute.Settings -> NavEntry(route) {
                    SettingsRoute(
                        onAction = { action ->
                            when (action) {
                                SettingsAction.NavigateBack -> backStack.removeLastOrNull()
                                SettingsAction.OpenNotificationSettings -> {
                                    backStack.add(AppRoute.NotificationSettings)
                                }

                                SettingsAction.OpenPhotoAccessPermission -> Unit

                                SettingsAction.OpenUsageGuide -> {
                                    backStack.add(AppRoute.UsageGuide)
                                }

                                SettingsAction.OpenDataManagement -> {
                                    backStack.add(AppRoute.DataManagement)
                                }

                                SettingsAction.OpenPrivacyGuide -> {
                                    backStack.add(AppRoute.PrivacyGuide)
                                }

                                SettingsAction.OpenOpenSourceLicenses -> {
                                    context.startActivity(
                                        Intent(context, OssLicensesMenuActivity::class.java),
                                    )
                                }

                                SettingsAction.OpenAccountManagement -> {
                                    backStack.add(AppRoute.AccountManagement)
                                }

                                SettingsAction.OpenContact,
                                -> Unit
                            }
                        },
                    )
                }

                AppRoute.NotificationSettings -> NavEntry(route) {
                    NotificationSettingsRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                AppRoute.UsageGuide -> NavEntry(route) {
                    UsageGuideScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onShareFavoriteGuideClick = {
                            backStack.add(AppRoute.ShareFavoriteGuide)
                        },
                    )
                }

                AppRoute.ShareFavoriteGuide -> NavEntry(route) {
                    OnboardingAddToFavoriteGuideScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                    )
                }

                AppRoute.DataManagement -> NavEntry(route) {
                    DataManagementRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                AppRoute.AccountManagement -> NavEntry(route) {
                    AccountManagementRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                AppRoute.PrivacyGuide -> NavEntry(route) {
                    PrivacyGuideScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onPrivacyPolicyClick = {},
                        onTermsClick = {},
                    )
                }

                AppRoute.Search -> NavEntry(route) {
                    SearchRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onNavigateToScreenshot = { captureId ->
                            if (captureId > 0) {
                                backStack.add(AppRoute.Screenshot(captureId))
                            }
                        },
                    )
                }

                AppRoute.RecentOrganizedScreenshots -> NavEntry(route) {
                    RecentOrganizedScreenshotsRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onNavigateToSearch = { backStack.add(AppRoute.Search) },
                        onNavigateToScreenshot = { captureId ->
                            if (captureId > 0) {
                                backStack.add(AppRoute.Screenshot(captureId))
                            }
                        },
                        onNavigateToOrganize = {
                            requestOpenOrganize = true
                            backStack.removeLastOrNull()
                        },
                    )
                }

                is AppRoute.Screenshot -> NavEntry(route) {
                    ScreenshotRoute(
                        captureId = route.captureId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onDeleteSucceeded = {
                            backStack.removeLastOrNull()
                        },
                    )
                }

                AppRoute.OrganizeAnalysisStatus -> NavEntry(route) {
                    renderedAnalysisStatus?.let { status ->
                        OrganizeAnalysisStatusRoute(
                            uiState = status,
                            onCancelClick = ::exitOrganizeAnalysisStatus,
                            onDismissClick = ::exitOrganizeAnalysisStatus,
                        )
                    }
                }

                else -> error("Unknown app route: $route")
            }
        },
    )
}

@Composable
fun RecapMainTabNavHost(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>,
    onNavigateToDeveloper: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecentOrganizedScreenshots: () -> Unit,
    onNavigateToOrganize: () -> Unit,
    onNavigateToCollectionFavorites: () -> Unit = {},
    onNavigateToScreenshot: (Long) -> Unit = {},
    openCollectionFavoritesOnEnter: Boolean = false,
    onOpenCollectionFavoritesOnEnterConsumed: () -> Unit = {},
    showDeveloperLogoShortcut: Boolean = false,
    onCollectionPredictiveBackProgress: (Float) -> Unit = {},
) {
    // Home ↔ Collection keeps its short slide+fade and bottom-bar predictive progress.
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        transitionSpec = { mainTabForwardTransition() },
        popTransitionSpec = { mainTabPopTransition() },
        predictivePopTransitionSpec = {
            EnterTransition.None togetherWith ExitTransition.None
        },
        entryProvider = { route ->
            when (route) {
                MainTabRoute.Home -> NavEntry(route) {
                    HomeRoute(
                        hazeState = hazeState,
                        onNavigateToDeveloper = onNavigateToDeveloper,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToSearch = onNavigateToSearch,
                        onNavigateToRecentOrganizedScreenshots = onNavigateToRecentOrganizedScreenshots,
                        onNavigateToCollectionFavorites = onNavigateToCollectionFavorites,
                        onNavigateToOrganize = onNavigateToOrganize,
                        onNavigateToScreenshot = onNavigateToScreenshot,
                        showDeveloperLogoShortcut = showDeveloperLogoShortcut,
                    )
                }

                MainTabRoute.Collection -> NavEntry(route) {
                    CollectionRoute(
                        hazeState = hazeState,
                        onNavigateToOrganize = onNavigateToOrganize,
                        onNavigateToSearch = onNavigateToSearch,
                        onNavigateToScreenshot = onNavigateToScreenshot,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        openCollectionFavoritesOnEnter = openCollectionFavoritesOnEnter,
                        onOpenCollectionFavoritesOnEnterConsumed =
                            onOpenCollectionFavoritesOnEnterConsumed,
                        onPredictiveBackProgress = onCollectionPredictiveBackProgress,
                    )
                }

                else -> error("Unknown main tab route: $route")
            }
        },
    )
}

private fun mainTabForwardTransition(): ContentTransform =
    slideInHorizontally(
        animationSpec = tween(MainTabSlideDurationMillis),
        initialOffsetX = { fullWidth -> fullWidth / MainTabSlideFraction },
    ) + fadeIn(
        animationSpec = tween(MainTabFadeDurationMillis),
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(MainTabSlideDurationMillis),
        targetOffsetX = { fullWidth -> -fullWidth / MainTabSlideFraction },
    ) + fadeOut(
        animationSpec = tween(MainTabFadeDurationMillis),
    )

private fun mainTabPopTransition(): ContentTransform =
    slideInHorizontally(
        animationSpec = tween(MainTabSlideDurationMillis),
        initialOffsetX = { fullWidth -> -fullWidth / MainTabSlideFraction },
    ) + fadeIn(
        animationSpec = tween(MainTabFadeDurationMillis),
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(MainTabSlideDurationMillis),
        targetOffsetX = { fullWidth -> fullWidth / MainTabSlideFraction },
    ) + fadeOut(
        animationSpec = tween(MainTabFadeDurationMillis),
    )
