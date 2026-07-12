package com.chalkak.recap.feature.screenshot

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.toLabelResId
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.chip.RecapCategoryChip
import com.chalkak.recap.core.design.component.chip.RecapCategoryChipColors
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisConfidence
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import com.chalkak.recap.core.model.screenshot.ScreenshotContentTypes

@Composable
fun ScreenshotDetailScreen(
    uiState: ScreenshotUiState,
    onAction: (ScreenshotAction) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onOpenMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WhiteStatusBarIconsEffect()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        when (uiState) {
            ScreenshotUiState.Loading -> ScreenshotDetailLoading()
            is ScreenshotUiState.NotFound -> ScreenshotDetailErrorState(
                message = stringResource(R.string.screenshot_detail_not_found),
                actionErrorMessageResId = uiState.actionErrorMessageResId,
                onRetry = { onAction(ScreenshotAction.RetryLoad) },
                onNavigateBack = onNavigateBack,
            )

            is ScreenshotUiState.LoadError -> ScreenshotDetailErrorState(
                message = stringResource(R.string.screenshot_detail_load_error),
                actionErrorMessageResId = null,
                onRetry = { onAction(ScreenshotAction.RetryLoad) },
                onNavigateBack = onNavigateBack,
            )

            is ScreenshotUiState.Content -> ScreenshotDetailContent(
                content = uiState,
                onAction = onAction,
                onNavigateBack = onNavigateBack,
                onOpenFullscreen = onOpenFullscreen,
                onOpenMore = onOpenMore,
            )
        }
    }
}

@Composable
private fun WhiteStatusBarIconsEffect() {
    val view = LocalView.current
    if (view.isInEditMode) {
        return
    }
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
            ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val previousLightStatusBars = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose {
            controller.isAppearanceLightStatusBars = previousLightStatusBars
        }
    }
}

@Composable
private fun ScreenshotDetailContent(
    content: ScreenshotUiState.Content,
    onAction: (ScreenshotAction) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onOpenMore: () -> Unit,
) {
    val card = content.card
    val analysis = card.analysisResult
    val imageModel = resolveScreenshotImageModel(
        storedImagePath = card.imageRefs.storedImagePath,
        sourceImageUri = card.imageRefs.sourceImageUri,
        thumbnailPath = card.imageRefs.thumbnailPath,
    )
    val contentType = analysis.contentTypes.primaryContentType
    val categoryType = contentType.toRecapCategoryType()
    val bodyText = analysis.body.ifBlank {
        stringResource(R.string.screenshot_body_empty)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenshotDetailHero(
            imageModel = imageModel,
            isFavorite = analysis.isFavorite,
            favoriteEnabled = !content.isFavoriteUpdating && !content.isDeleting,
            onNavigateBack = onNavigateBack,
            onFavoriteClick = { onAction(ScreenshotAction.ToggleFavorite) },
            onMoreClick = onOpenMore,
            onFullscreenClick = onOpenFullscreen,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = ScreenshotTokens.HorizontalPadding,
                    top = ScreenshotTokens.ContentTopPadding,
                    end = ScreenshotTokens.HorizontalPadding,
                    bottom = ScreenshotTokens.ContentBottomPadding,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (categoryType != null) {
                    RecapCategoryChip(type = categoryType)
                } else {
                    RecapCategoryChip(
                        label = stringResource(contentType.toLabelResId()),
                        colors = RecapCategoryChipColors(
                            border = RecapGray300,
                            content = RecapGray900,
                        ),
                    )
                }
                Text(
                    text = stringResource(
                        R.string.screenshot_organized_date_format,
                        formatOrganizedDate(card.createdAtMillis),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = RecapGray200,
                )
            }
            Text(
                text = analysis.title,
                modifier = Modifier.padding(top = ScreenshotTokens.SectionSpacing),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
            )
            Text(
                text = analysis.summary,
                modifier = Modifier.padding(top = ScreenshotTokens.TitleToSummarySpacing),
                style = MaterialTheme.typography.bodyLarge,
                color = RecapGray700,
            )
            Text(
                text = bodyText,
                modifier = Modifier.padding(top = ScreenshotTokens.SummaryToBodySpacing),
                style = MaterialTheme.typography.bodyMedium,
                color = RecapGray700,
            )
            content.actionErrorMessageResId?.let { errorResId ->
                Text(
                    text = stringResource(errorResId),
                    modifier = Modifier.padding(top = ScreenshotTokens.SectionSpacing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RecapError,
                )
            }
        }
    }
}

@Composable
private fun ScreenshotDetailHero(
    imageModel: Any?,
    isFavorite: Boolean,
    favoriteEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit,
    onFullscreenClick: () -> Unit,
) {
    var imageLoadFailed by remember(imageModel) { mutableStateOf(false) }
    val showPlaceholder = imageModel == null || imageLoadFailed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ScreenshotTokens.HeroHeight),
    ) {
        if (showPlaceholder) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = RecapGray100,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.screenshot_image_load_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RecapGray500,
                        modifier = Modifier.padding(ScreenshotTokens.HorizontalPadding),
                    )
                }
            }
        } else {
            AsyncImage(
                model = imageModel,
                contentDescription = stringResource(
                    R.string.screenshot_image_placeholder_content_description,
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { imageLoadFailed = true },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(ScreenshotTokens.HeroGradientHeightFraction)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                    ),
                )
                .alpha(0.9f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = ScreenshotTokens.OverlayHorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScreenshotIconButton(
                    iconResId = R.drawable.ic_chevron_left_24,
                    contentDescription = stringResource(
                        R.string.screenshot_detail_back_content_description,
                    ),
                    onClick = onNavigateBack,
                    tint = White,
                )
                Text(
                    text = stringResource(R.string.screenshot_detail_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = White,
                )
            }
            Row {
                ScreenshotIconButton(
                    iconResId = R.drawable.ic_star_24,
                    contentDescription = stringResource(
                        if (isFavorite) {
                            R.string.screenshot_detail_favorite_selected_content_description
                        } else {
                            R.string.screenshot_detail_favorite_content_description
                        },
                    ),
                    onClick = onFavoriteClick,
                    enabled = favoriteEnabled,
                    checked = isFavorite,
                    tint = if (isFavorite) {
                        RecapGray200
                    } else {
                        White
                    },
                )
                ScreenshotIconButton(
                    iconResId = R.drawable.ic_more_24,
                    contentDescription = stringResource(
                        R.string.screenshot_detail_more_content_description,
                    ),
                    onClick = onMoreClick,
                    tint = White,
                )
            }
        }

        ScreenshotIconButton(
            iconResId = R.drawable.ic_fullscreen_24,
            contentDescription = stringResource(
                R.string.screenshot_detail_fullscreen_content_description,
            ),
            onClick = onFullscreenClick,
            tint = RecapGray500,
            outlined = true,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(ScreenshotTokens.FullscreenButtonPadding),
        )
    }
}

@Composable
internal fun ScreenshotIconButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checked: Boolean? = null,
    tint: Color = RecapGray500,
    outlined: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickModifier = if (checked != null) {
        Modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Checkbox,
            interactionSource = interactionSource,
            indication = null,
            onValueChange = { onClick() },
        )
    } else {
        Modifier.clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick,
        )
    }
    if (outlined) {
        val shape = RoundedCornerShape(ScreenshotTokens.IconButtonCornerRadius)
        Box(
            modifier = modifier
                .size(ScreenshotTokens.IconButtonSize)
                .background(color = White, shape = shape)
                .border(
                    width = ScreenshotTokens.IconButtonBorderWidth,
                    color = RecapGray200,
                    shape = shape,
                )
                .then(clickModifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(ScreenshotTokens.IconButtonIconSize),
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(ScreenshotTokens.IconTouchTarget)
                .then(clickModifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(ScreenshotTokens.IconSize),
            )
        }
    }
}

@Composable
private fun ScreenshotDetailLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ScreenshotDetailErrorState(
    message: String,
    actionErrorMessageResId: Int?,
    onRetry: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(ScreenshotTokens.HorizontalPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = RecapGray900,
        )
        actionErrorMessageResId?.let { errorResId ->
            Text(
                text = stringResource(errorResId),
                modifier = Modifier.padding(top = ScreenshotTokens.MetaRowSpacing),
                style = MaterialTheme.typography.bodyMedium,
                color = RecapError,
            )
        }
        Spacer(modifier = Modifier.height(ScreenshotTokens.ErrorStateSpacing))
        RecapButton(
            text = stringResource(R.string.screenshot_detail_retry),
            onClick = onRetry,
            size = RecapButtonSize.Medium,
        )
        Spacer(modifier = Modifier.height(ScreenshotTokens.MetaRowSpacing))
        RecapButton(
            text = stringResource(R.string.screenshot_action_close),
            onClick = onNavigateBack,
            size = RecapButtonSize.Medium,
            colors = RecapButtonDefaults.outlinedColors(),
        )
    }
}

internal fun previewScreenshotContent(
    title: String = "제주 숙소 예약 정보",
    summary: String = "체크인 15:00, 체크아웃 11:00",
    body: String = "예약 번호 ABC-1234\n게스트 2명",
    contentType: ScreenshotContentType = ScreenshotContentType.PLACE_RESTAURANT,
    isFavorite: Boolean = false,
): ScreenshotUiState.Content {
    val analysis = ScreenshotAnalysisResult(
        imageId = "preview-image-id",
        title = title,
        summary = summary,
        contentTypes = ScreenshotContentTypes(primaryContentType = contentType),
        keyFields = emptyList(),
        confidence = ScreenshotAnalysisConfidence.HIGH,
        isFavorite = isFavorite,
        body = body,
    )
    return ScreenshotUiState.Content(
        card = StoredScreenshotCard(
            analysisResult = analysis,
            imageRefs = ScreenshotCardImageRefs(),
            createdAtMillis = 1_720_000_000_000L,
            updatedAtMillis = 1_720_000_000_000L,
        ),
        editDraft = ScreenshotEditDraft(
            title = analysis.title,
            summary = analysis.summary,
            body = analysis.body,
            contentType = analysis.contentTypes.primaryContentType,
        ),
    )
}

@Preview(name = "Screenshot Detail Content", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ScreenshotDetailContentPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotDetailScreen(
            uiState = previewScreenshotContent(),
            onAction = {},
            onNavigateBack = {},
            onOpenEdit = {},
            onOpenFullscreen = {},
            onOpenMore = {},
        )
    }
}

@Preview(name = "Screenshot Detail Loading", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ScreenshotDetailLoadingPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotDetailScreen(
            uiState = ScreenshotUiState.Loading,
            onAction = {},
            onNavigateBack = {},
            onOpenEdit = {},
            onOpenFullscreen = {},
            onOpenMore = {},
        )
    }
}

@Preview(name = "Screenshot Detail Error", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ScreenshotDetailErrorPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotDetailScreen(
            uiState = ScreenshotUiState.LoadError(),
            onAction = {},
            onNavigateBack = {},
            onOpenEdit = {},
            onOpenFullscreen = {},
            onOpenMore = {},
        )
    }
}

@Preview(
    name = "Screenshot Detail Empty Body",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ScreenshotDetailEmptyBodyPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotDetailScreen(
            uiState = previewScreenshotContent(body = ""),
            onAction = {},
            onNavigateBack = {},
            onOpenEdit = {},
            onOpenFullscreen = {},
            onOpenMore = {},
        )
    }
}
