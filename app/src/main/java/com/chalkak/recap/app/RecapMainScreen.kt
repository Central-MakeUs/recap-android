package com.chalkak.recap.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
fun RecapMainScreen(
    viewModel: RecapMainViewModel = viewModel(),
    onNavigateToDeveloper: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(RecapRoute.Home)
    val currentRoute = backStack.lastOrNull() as? RecapRoute ?: RecapRoute.Home

    LaunchedEffect(currentRoute) {
        viewModel.onDestinationChanged(currentRoute)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                RecapRoute.topLevelRoutes.forEach { route ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            viewModel.onRouteSelected(route)
                            if (currentRoute != route) {
                                backStack.remove(route)
                                backStack.add(route)
                            }
                        },
                        icon = {
                            RouteIndicator(selected = currentRoute == route)
                        },
                        label = {
                            Text(stringResource(route.labelResId))
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        RecapNavHost(
            backStack = backStack,
            onNavigateToDeveloper = onNavigateToDeveloper,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun RouteIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = modifier
            .size(if (selected) 10.dp else 6.dp)
            .clip(CircleShape)
            .background(color),
    )
}
