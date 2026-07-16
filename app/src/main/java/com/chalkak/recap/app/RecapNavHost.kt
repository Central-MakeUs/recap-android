package com.chalkak.recap.app

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.chalkak.recap.feature.collection.CollectionRoute
import com.chalkak.recap.feature.home.HomeAnalysisProgressUiModel
import com.chalkak.recap.feature.home.HomeRoute
import com.chalkak.recap.feature.home.RecentOrganizedScreenshotsRoute
import com.chalkak.recap.feature.home.SearchRoute
import com.chalkak.recap.feature.settings.AccountManagementRoute
import com.chalkak.recap.feature.settings.DataManagementRoute
import com.chalkak.recap.feature.settings.NotificationSettingsRoute
import com.chalkak.recap.feature.settings.SettingsAction
import com.chalkak.recap.feature.settings.SettingsRoute
import com.chalkak.recap.feature.onboarding.screen.OnboardingAddToFavoriteGuideScreen
import com.chalkak.recap.feature.settings.screen.PrivacyGuideScreen
import com.chalkak.recap.feature.settings.screen.UsageGuideScreen
import com.chalkak.recap.feature.screenshot.ScreenshotRoute
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(AppRoute.MainTabs)
    val analysisProgressViewModel: ScreenshotAnalysisProgressViewModel = hiltViewModel()
    var homeNavigationRequestId by remember { mutableIntStateOf(0) }
    var requestOpenOrganize by remember { mutableStateOf(false) }
    val analysisProgressFlow = remember(analysisProgressViewModel) {
        analysisProgressViewModel.uiState.map { state ->
            HomeAnalysisProgressUiModel(
                isRunning = state.isRunning,
                progress = state.progress,
            )
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
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
                                analysisProgressViewModel.startMockAnalysis(selectedScreenshots)
                                homeNavigationRequestId += 1
                            },
                            onNavigateToScreenshot = { imageId ->
                                if (imageId.isNotBlank()) {
                                    backStack.add(AppRoute.Screenshot(imageId))
                                }
                            },
                            homeNavigationRequestId = homeNavigationRequestId,
                            pendingOpenOrganize = pendingOpenOrganize || requestOpenOrganize,
                            onPendingOpenOrganizeConsumed = {
                                if (requestOpenOrganize) {
                                    requestOpenOrganize = false
                                } else {
                                    onPendingOpenOrganizeConsumed()
                                }
                            },
                            analysisProgressFlow = analysisProgressFlow,
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

                                SettingsAction.OpenPhotoAccessPermission -> {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null),
                                        ),
                                    )
                                }

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
                    )
                }

                AppRoute.RecentOrganizedScreenshots -> NavEntry(route) {
                    RecentOrganizedScreenshotsRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onNavigateToSearch = { backStack.add(AppRoute.Search) },
                        onNavigateToScreenshot = { imageId ->
                            if (imageId.isNotBlank()) {
                                backStack.add(AppRoute.Screenshot(imageId))
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
                        imageId = route.imageId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
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
    onNavigateToScreenshot: (String) -> Unit = {},
    collectionFavoritesNavigationRequestId: Int = 0,
    showDeveloperLogoShortcut: Boolean = false,
    analysisProgressFlow: Flow<HomeAnalysisProgressUiModel> = flowOf(HomeAnalysisProgressUiModel()),
    onCollectionPredictiveBackProgress: (Float) -> Unit = {},
) {
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
                        analysisProgressFlow = analysisProgressFlow,
                    )
                }

                MainTabRoute.Collection -> NavEntry(route) {
                    CollectionRoute(
                        hazeState = hazeState,
                        onNavigateToOrganize = onNavigateToOrganize,
                        onNavigateToScreenshot = onNavigateToScreenshot,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        favoritesNavigationRequestId = collectionFavoritesNavigationRequestId,
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
