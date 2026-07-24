package com.chalkak.recap.core.design.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneInfo
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberSceneState
import androidx.navigationevent.NavigationEventTransitionState.Idle
import androidx.navigationevent.NavigationEventTransitionState.InProgress
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch

/**
 * [RecapNavigationMotion]을 적용하는 single-pane Navigation3 호스트.
 * predictive 제스처 scrub과 commit completion을 분리한다.
 *
 * predictive 제스처 중에는 공유 [RecapNavigationMotion.pop] transform을
 * [RecapNavigationMotion.PredictiveMaxFraction]까지만 seek한다.
 * commit 시 남은 거리를 애니한 뒤 `onBack`으로 back stack을 한 번 pop한다.
 *
 * 스택 전체 교체(첫 entry 변경)는 [RecapNavigationMotion.none]을 사용해
 * root Onboarding ↔ Main 교체를 무전환으로 두고, Main ↔ Developer push/pop에는 영향을 주지 않는다.
 */
@Composable
fun <T : Any> RecapNavDisplay(
    backStack: List<T>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    predictivePopEnabled: Boolean = true,
    onPredictiveProgress: (Float) -> Unit = {},
    entryDecorators: List<NavEntryDecorator<T>> = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
    ),
    contentAlignment: Alignment = Alignment.TopStart,
    transitionSpec: () -> ContentTransform = { RecapNavigationMotion.forward() },
    popTransitionSpec: () -> ContentTransform = { RecapNavigationMotion.pop() },
    entryProvider: (T) -> NavEntry<T>,
) {
    require(backStack.isNotEmpty()) { "RecapNavDisplay backStack cannot be empty" }

    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = entryDecorators,
        entryProvider = entryProvider,
    )
    val sceneState = rememberSceneState(
        entries = entries,
        sceneStrategies = listOf(SinglePaneSceneStrategy()),
        onBack = onBack,
    )
    val scene = sceneState.currentScene
    val previousScene = sceneState.previousScenes.lastOrNull()

    val transitionState = remember { SeekableTransitionState(scene) }
    val transition = rememberTransition(transitionState, label = "recapNavScene")
    val scope = rememberCoroutineScope()
    var isCommitting by remember { mutableStateOf(false) }

    val gestureState = rememberNavigationEventState(
        currentInfo = SceneInfo(scene),
        backInfo = sceneState.previousScenes.map { SceneInfo(it) },
    )
    val gestureTransition = gestureState.transitionState
    val inPredictiveBack =
        predictivePopEnabled &&
            !isCommitting &&
            gestureTransition is InProgress &&
            previousScene != null
    val gestureProgress = when (gestureTransition) {
        is Idle -> 0f
        is InProgress -> gestureTransition.latestEvent.progress
    }

    LaunchedEffect(gestureProgress, inPredictiveBack, isCommitting) {
        when {
            inPredictiveBack -> onPredictiveProgress(gestureProgress)
            isCommitting -> Unit
            else -> onPredictiveProgress(0f)
        }
    }

    NavigationBackHandler(
        state = gestureState,
        isBackEnabled = scene.previousEntries.isNotEmpty() && !isCommitting,
        onBackCancelled = {
            scope.launch {
                onPredictiveProgress(0f)
                val durationMillis = (
                    transitionState.fraction * RecapNavigationMotion.SlideDurationMillis
                    ).toInt().coerceAtLeast(0)
                animate(
                    initialValue = transitionState.fraction,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis),
                ) { value, _ ->
                    launch { transitionState.seekTo(value) }
                }
                transitionState.snapTo(scene)
            }
        },
        onBackCompleted = {
            if (!predictivePopEnabled || previousScene == null) {
                onBack()
                return@NavigationBackHandler
            }
            scope.launch {
                isCommitting = true
                onPredictiveProgress(1f)
                val startFraction = transitionState.fraction.coerceIn(
                    0f,
                    RecapNavigationMotion.PredictiveMaxFraction,
                )
                if (transitionState.targetState != previousScene) {
                    transitionState.seekTo(startFraction, previousScene)
                }
                val durationMillis = (
                    (1f - startFraction) * RecapNavigationMotion.SlideDurationMillis
                    ).toInt().coerceAtLeast(0)
                animate(
                    initialValue = startFraction,
                    targetValue = 1f,
                    animationSpec = tween(durationMillis),
                ) { value, _ ->
                    launch { transitionState.seekTo(value, previousScene) }
                }
                onBack()
                transitionState.snapTo(previousScene)
                onPredictiveProgress(0f)
                isCommitting = false
            }
        },
    )

    val previousEntryKeys = remember { mutableStateOf(sceneState.entries.map { it.contentKey }) }
    val navigationKind = remember(sceneState.entries) {
        val oldBackStack = previousEntryKeys.value
        val newBackStack = sceneState.entries.map { it.contentKey }
        val kind = classifyRecapNavigation(oldBackStack, newBackStack)
        previousEntryKeys.value = newBackStack
        kind
    }

    val predictiveTarget = previousScene.takeIf { inPredictiveBack }
    if (predictiveTarget != null) {
        LaunchedEffect(predictiveTarget, gestureProgress) {
            transitionState.seekTo(
                RecapNavigationMotionOffsets.previewPopFraction(gestureProgress),
                predictiveTarget,
            )
        }
    } else if (!isCommitting) {
        LaunchedEffect(scene) {
            if (transitionState.currentState != scene) {
                transitionState.animateTo(scene)
            } else if (transitionState.targetState != scene) {
                transitionState.snapTo(scene)
            }
        }
    }

    transition.AnimatedContent(
        contentKey = { target -> target.key },
        contentAlignment = contentAlignment,
        modifier = modifier,
        transitionSpec = {
            val transform = when {
                inPredictiveBack || isCommitting -> popTransitionSpec()
                navigationKind == RecapNavigationKind.Replace -> RecapNavigationMotion.none()
                navigationKind == RecapNavigationKind.Pop -> popTransitionSpec()
                else -> transitionSpec()
            }
            ContentTransform(
                targetContentEnter = transform.targetContentEnter,
                initialContentExit = transform.initialContentExit,
                targetContentZIndex = when {
                    navigationKind == RecapNavigationKind.Pop ||
                        inPredictiveBack ||
                        isCommitting -> -1f
                    else -> 1f
                },
            )
        },
    ) { targetScene ->
        targetScene.content()
    }
}

private enum class RecapNavigationKind {
    Forward,
    Pop,
    Replace,
}

private fun <T : Any> classifyRecapNavigation(
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
