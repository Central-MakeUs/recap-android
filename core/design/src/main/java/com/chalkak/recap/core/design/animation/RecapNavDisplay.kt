package com.chalkak.recap.core.design.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/**
 * 공식 Navigation3 [NavDisplay] 위에 RECAP 공통 motion 기본값을 얹은 thin wrapper.
 *
 * - push/pop: [RecapNavigationMotion.forward] / [RecapNavigationMotion.pop]
 * - predictive: edge gesture는 full-range [RecapNavigationMotion.pop],
 *   3버튼/하드웨어 back(`EDGE_NONE`)은 [RecapNavigationMotion.none]
 * - 스택 root 교체(Replace): Onboarding ↔ Main 등에서 무전환
 *
 * scrub / cancel / commit 소유권은 공식 [NavDisplay]에 둔다.
 */
@Composable
fun <T : Any> RecapNavDisplay(
    backStack: List<T>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    predictivePopEnabled: Boolean = true,
    entryDecorators: List<NavEntryDecorator<T>> = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
    ),
    contentAlignment: Alignment = Alignment.TopStart,
    transitionSpec: () -> ContentTransform = { RecapNavigationMotion.forward() },
    popTransitionSpec: () -> ContentTransform = { RecapNavigationMotion.pop() },
    predictivePopSpec: ((swipeEdge: Int) -> ContentTransform)? = null,
    entryProvider: (T) -> NavEntry<T>,
) {
    require(backStack.isNotEmpty()) { "RecapNavDisplay backStack cannot be empty" }

    val previousEntryKeys = remember { mutableStateOf(backStack.toList()) }
    val navigationKind = remember(backStack) {
        val kind = classifyRecapNavigation(previousEntryKeys.value, backStack)
        previousEntryKeys.value = backStack.toList()
        kind
    }

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier,
        entryDecorators = entryDecorators,
        contentAlignment = contentAlignment,
        transitionSpec = {
            if (navigationKind == RecapNavigationKind.Replace) {
                RecapNavigationMotion.none()
            } else {
                transitionSpec()
            }
        },
        popTransitionSpec = {
            if (navigationKind == RecapNavigationKind.Replace) {
                RecapNavigationMotion.none()
            } else {
                popTransitionSpec()
            }
        },
        predictivePopTransitionSpec = { swipeEdge ->
            when {
                !predictivePopEnabled -> RecapNavigationMotion.none()
                navigationKind == RecapNavigationKind.Replace -> RecapNavigationMotion.none()
                predictivePopSpec != null -> predictivePopSpec(swipeEdge)
                else -> RecapNavigationMotion.predictivePop(swipeEdge)
            }
        },
        entryProvider = entryProvider,
    )
}

internal enum class RecapNavigationKind {
    Forward,
    Pop,
    Replace,
}

internal fun <T : Any> classifyRecapNavigation(
    oldBackStack: List<T>,
    newBackStack: List<T>,
): RecapNavigationKind {
    if (oldBackStack.isEmpty() || newBackStack.isEmpty()) return RecapNavigationKind.Replace
    if (oldBackStack.first() != newBackStack.first()) return RecapNavigationKind.Replace
    if (newBackStack.size > oldBackStack.size) return RecapNavigationKind.Forward
    val divergingIndex = newBackStack.indices.firstOrNull { index ->
        newBackStack[index] != oldBackStack[index]
    }
    return if (divergingIndex == null && newBackStack.size != oldBackStack.size) {
        RecapNavigationKind.Pop
    } else if (newBackStack.size < oldBackStack.size) {
        RecapNavigationKind.Pop
    } else {
        RecapNavigationKind.Forward
    }
}
