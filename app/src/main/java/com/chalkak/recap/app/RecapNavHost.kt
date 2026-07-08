package com.chalkak.recap.app

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.feature.collection.CollectionRoute
import com.chalkak.recap.feature.organize.OrganizeRoute
import com.chalkak.recap.feature.home.HomeAnalysisProgressUiModel
import com.chalkak.recap.feature.home.HomeRoute
import com.chalkak.recap.feature.home.SearchRoute
import com.chalkak.recap.feature.mypage.MyPageAction
import dev.chrisbanes.haze.HazeState
import com.chalkak.recap.feature.mypage.MyPageDataManagementScreen
import com.chalkak.recap.feature.mypage.MyPageNotificationSettingsRoute
import com.chalkak.recap.feature.mypage.MyPagePrivacyGuideScreen
import com.chalkak.recap.feature.mypage.MyPageScreen
import com.chalkak.recap.feature.mypage.MyPageServiceInfoScreen
import com.chalkak.recap.feature.mypage.MyPageUploadGuideScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Composable
fun RecapNavHost(
    onNavigateToDeveloper: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(AppRoute.MainTabs)
    val analysisProgressViewModel: ScreenshotAnalysisProgressViewModel = hiltViewModel()
    var homeNavigationRequestId by remember { mutableIntStateOf(0) }
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
                    RecapMainScreen(
                        onNavigateToDeveloper = onNavigateToDeveloper,
                        onNavigateToMyPage = { backStack.add(AppRoute.MyPage) },
                        onNavigateToSearch = { backStack.add(AppRoute.Search) },
                        onNavigateToOrganize = { backStack.add(AppRoute.Organize) },
                        homeNavigationRequestId = homeNavigationRequestId,
                        analysisProgressFlow = analysisProgressFlow,
                    )
                }

                AppRoute.MyPage -> NavEntry(route) {
                    MyPageScreen(
                        isDebugBuild = BuildConfig.DEBUG,
                        onAction = { action ->
                            when (action) {
                                MyPageAction.NavigateBack -> backStack.removeLastOrNull()
                                MyPageAction.OpenNotificationSettings -> {
                                    backStack.add(AppRoute.MyPageNotificationSettings)
                                }

                                MyPageAction.OpenUploadGuide -> {
                                    backStack.add(AppRoute.MyPageUploadGuide)
                                }

                                MyPageAction.OpenDataManagement -> {
                                    backStack.add(AppRoute.MyPageDataManagement)
                                }

                                MyPageAction.OpenPrivacyGuide -> {
                                    backStack.add(AppRoute.MyPagePrivacyGuide)
                                }

                                MyPageAction.OpenServiceInfo -> {
                                    backStack.add(AppRoute.MyPageServiceInfo)
                                }

                                else -> Unit
                            }
                        },
                    )
                }

                AppRoute.MyPageNotificationSettings -> NavEntry(route) {
                    MyPageNotificationSettingsRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                AppRoute.MyPageUploadGuide -> NavEntry(route) {
                    MyPageUploadGuideScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onOpenSettingsClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null),
                                ),
                            )
                        },
                    )
                }

                AppRoute.MyPageDataManagement -> NavEntry(route) {
                    MyPageDataManagementScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onAccountManagementClick = {},
                    )
                }

                AppRoute.MyPagePrivacyGuide -> NavEntry(route) {
                    MyPagePrivacyGuideScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onPrivacyPolicyClick = {},
                        onTermsClick = {},
                    )
                }

                AppRoute.MyPageServiceInfo -> NavEntry(route) {
                    MyPageServiceInfoScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onContactClick = {},
                        onNoticeClick = {},
                        onTermsClick = {},
                        onPrivacyPolicyClick = {},
                        onOpenSourceLicenseClick = {},
                    )
                }

                AppRoute.Search -> NavEntry(route) {
                    SearchRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                    )
                }

                AppRoute.Organize -> NavEntry(route) {
                    OrganizeRoute(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onOrganizeComplete = { selectedScreenshots ->
                            analysisProgressViewModel.startMockAnalysis(selectedScreenshots)
                            backStack.removeLastOrNull()
                            homeNavigationRequestId += 1
                        },
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
    onNavigateToOrganize: () -> Unit,
    analysisProgressFlow: Flow<HomeAnalysisProgressUiModel> = flowOf(HomeAnalysisProgressUiModel()),
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = { route ->
            when (route) {
                MainTabRoute.Home -> NavEntry(route) {
                    HomeRoute(
                        hazeState = hazeState,
                        onNavigateToDeveloper = onNavigateToDeveloper,
                        analysisProgressFlow = analysisProgressFlow,
                    )
                }

                MainTabRoute.Collection -> NavEntry(route) {
                    CollectionRoute(
                        hazeState = hazeState,
                        onNavigateToOrganize = onNavigateToOrganize,
                    )
                }

                else -> error("Unknown main tab route: $route")
            }
        },
    )
}
