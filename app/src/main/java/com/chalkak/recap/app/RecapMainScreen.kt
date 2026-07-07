package com.chalkak.recap.app

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.topbar.RecapMainTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun RecapMainScreen(
    onNavigateToDeveloper: () -> Unit = {},
    onNavigateToMyPage: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(MainTabRoute.Home)
    val currentRoute = backStack.lastOrNull() as? MainTabRoute ?: MainTabRoute.Home

    fun navigateTo(route: MainTabRoute) {
        if (backStack.lastOrNull() != route) {
            backStack.clear()
            backStack.add(route)
        }
    }

    Scaffold(
        topBar = {
            RecapMainTopBar(
                onSettingsClick = onNavigateToMyPage,
                onSearchClick = onNavigateToSearch,
            )
        },
        bottomBar = {
            RecapBottomBar(
                currentRoute = currentRoute,
                onRouteClick = ::navigateTo,
                onCleanupClick = {},
            )
        },
    ) { innerPadding ->
        RecapMainTabNavHost(
            backStack = backStack,
            onNavigateToDeveloper = onNavigateToDeveloper,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun RecapBottomBar(
    currentRoute: MainTabRoute,
    onRouteClick: (MainTabRoute) -> Unit,
    onCleanupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    RecapBottomBarHeight +
                        RecapCleanupButtonProtrusion +
                        RecapBottomBarBottomPadding,
                ),
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = RecapBottomBarHorizontalPadding,
                        end = RecapBottomBarHorizontalPadding,
                        bottom = RecapBottomBarBottomPadding,
                    ),
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RecapBottomBarHeight)
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RecapBottomBarItem(
                        labelResId = R.string.bottom_nav_home,
                        icon = rememberVectorPainter(Icons.Outlined.Home),
                        selected = currentRoute == MainTabRoute.Home,
                        onClick = { onRouteClick(MainTabRoute.Home) },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RecapBottomBarItem(
                        labelResId = R.string.bottom_nav_collection,
                        icon = painterResource(R.drawable.ic_storage_24),
                        selected = currentRoute == MainTabRoute.Collection,
                        onClick = { onRouteClick(MainTabRoute.Collection) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            RecapCleanupBottomBarItem(
                onClick = onCleanupClick,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(96.dp),
            )
        }
    }
}

@Composable
private fun RecapBottomBarItem(
    @StringRes labelResId: Int,
    icon: Painter,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(shape = RoundedCornerShape(percent = 25))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = contentColor,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(labelResId),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RecapCleanupBottomBarItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.outlineVariant,
                )
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus_30),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.White,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.bottom_nav_cleanup),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

private val RecapBottomBarHeight: Dp = 80.dp
private val RecapCleanupButtonProtrusion: Dp = 8.dp
private val RecapBottomBarHorizontalPadding: Dp = 16.dp
private val RecapBottomBarBottomPadding: Dp = 16.dp

@Preview(name = "Main Bottom Bar", showBackground = true, widthDp = 360)
@Composable
private fun RecapBottomBarPreview() {
    RECAPTheme {
        RecapBottomBar(
            currentRoute = MainTabRoute.Home,
            onRouteClick = {},
            onCleanupClick = {},
        )
    }
}
