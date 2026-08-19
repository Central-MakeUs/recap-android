package com.chalkak.recap.core.design.component.swipe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.card.ScreenshotCard
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapErrorContainer
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray700

@Immutable
class ScreenshotCardSwipeScope internal constructor(
    val isGestureActive: Boolean,
    private val actions: List<SwipeRevealAction>,
) {
    fun Modifier.screenshotCardSwipeSemantics(): Modifier = semantics {
        customActions = actions.map { action ->
            CustomAccessibilityAction(label = action.label) {
                action.onClick()
                true
            }
        }
    }
}

@Composable
fun ScreenshotCardSwipeRow(
    revealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDragStarted: () -> Unit = {},
    content: @Composable ScreenshotCardSwipeScope.() -> Unit,
) {
    val currentOnEditClick = rememberUpdatedState(onEditClick)
    val currentOnDeleteClick = rememberUpdatedState(onDeleteClick)
    val editLabel = stringResource(R.string.screenshot_card_swipe_edit)
    val deleteLabel = stringResource(R.string.screenshot_card_swipe_delete)
    val actions = remember(editLabel, deleteLabel) {
        listOf(
            SwipeRevealAction(
                label = editLabel,
                containerColor = RecapGray50,
                contentColor = RecapGray700,
                onClick = { currentOnEditClick.value() },
            ),
            SwipeRevealAction(
                label = deleteLabel,
                containerColor = RecapErrorContainer,
                contentColor = RecapError,
                onClick = { currentOnDeleteClick.value() },
            ),
        )
    }
    SwipeRevealRow(
        actions = actions,
        revealed = revealed,
        onRevealedChange = onRevealedChange,
        modifier = modifier,
        enabled = enabled,
        onDragStarted = onDragStarted,
    ) { isGestureActive ->
        val scope = remember(isGestureActive, actions) {
            ScreenshotCardSwipeScope(isGestureActive, actions)
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            scope.content()
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = RecapGray100,
            )
        }
    }
}

@Preview(name = "Screenshot card swipe row revealed", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotCardSwipeRowPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCardSwipeRow(
            revealed = true,
            onRevealedChange = {},
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(24.dp),
        ) {
            ScreenshotCard(
                thumbnailModel = R.drawable.bid_landscape_24px,
                categoryType = RecapCategoryType.ScheduleReservation,
                title = "파스타 레시피 저장",
                description = "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약",
                isFavorite = false,
                onClick = {},
                onFavoriteClick = {},
                suppressPressScale = isGestureActive,
                modifier = Modifier.screenshotCardSwipeSemantics(),
            )
        }
    }
}
