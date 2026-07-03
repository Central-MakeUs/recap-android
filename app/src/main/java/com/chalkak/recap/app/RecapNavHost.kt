package com.chalkak.recap.app

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chalkak.recap.feature.collection.CollectionScreen
import com.chalkak.recap.feature.home.HomeRoute
import com.chalkak.recap.feature.mypage.MyPageAction
import com.chalkak.recap.feature.mypage.MyPageDataManagementScreen
import com.chalkak.recap.feature.mypage.MyPageNotificationSettingsScreen
import com.chalkak.recap.feature.mypage.MyPagePrivacyGuideScreen
import com.chalkak.recap.feature.mypage.MyPageScreen
import com.chalkak.recap.feature.mypage.MyPageServiceInfoScreen
import com.chalkak.recap.feature.mypage.MyPageUploadGuideScreen

@Composable
fun RecapNavHost(
    onNavigateToDeveloper: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(AppRoute.MainTabs)

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
                    )
                }

                AppRoute.MyPage -> NavEntry(route) {
                    MyPageScreen(
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
                    MyPageNotificationSettingsScreen(
                        onBackClick = { backStack.removeLastOrNull() },
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

                else -> error("Unknown app route: $route")
            }
        },
    )
}

@Composable
fun RecapMainTabNavHost(
    backStack: NavBackStack<NavKey>,
    onNavigateToDeveloper: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = { route ->
            when (route) {
                MainTabRoute.Home -> NavEntry(route) {
                    HomeRoute(onNavigateToDeveloper = onNavigateToDeveloper)
                }

                MainTabRoute.Collection -> NavEntry(route) {
                    CollectionScreen()
                }

                else -> error("Unknown main tab route: $route")
            }
        },
    )
}
