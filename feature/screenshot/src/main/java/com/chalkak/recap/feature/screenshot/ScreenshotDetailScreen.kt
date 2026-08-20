package com.chalkak.recap.feature.screenshot

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.data.screenshot.persistence.ScreenshotCardImageRefs
import com.chalkak.recap.core.data.screenshot.persistence.StoredScreenshotCard
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.toRecapCategoryType
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.chip.RecapCategoryTextChip
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapImagePlaceholderBackground
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading2
import com.chalkak.recap.core.design.theme.White
import com.chalkak.recap.core.model.screenshot.ScreenshotAnalysisResult
import com.chalkak.recap.core.model.screenshot.ScreenshotContentType
import java.time.Instant

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ScreenshotDetailScreen(
    uiState: ScreenshotUiState,
    onAction: (ScreenshotAction) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onOpenMore: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    enableSharedImageBounds: Boolean = false,
    fullscreenOwnsSharedImageRaster: Boolean = false,
) {
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
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                enableSharedImageBounds = enableSharedImageBounds,
                fullscreenOwnsSharedImageRaster = fullscreenOwnsSharedImageRaster,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ScreenshotDetailContent(
    content: ScreenshotUiState.Content,
    onAction: (ScreenshotAction) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenFullscreen: () -> Unit,
    onOpenMore: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    enableSharedImageBounds: Boolean,
    fullscreenOwnsSharedImageRaster: Boolean,
) {
    val card = content.card
    val analysis = card.analysisResult
    val imageModel = rememberBoundedScreenshotImageRequest(
        resolveScreenshotImageModel(
            storedImagePath = card.imageRefs.storedImagePath,
            sourceImageUri = card.imageRefs.sourceImageUri,
            thumbnailPath = card.imageRefs.thumbnailPath,
            priority = ScreenshotImageResolvePriority.Fullscreen,
        ),
    )
    val contentType = analysis.typeCode
    val categoryType = contentType.toRecapCategoryType()
    val bodyText = analysis.body.ifBlank {
        stringResource(R.string.screenshot_body_empty)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        ScreenshotDetailTopBar(
            isFavorite = analysis.isFavorite,
            favoriteEnabled = !content.isFavoriteUpdating && !content.isDeleting,
            onNavigateBack = onNavigateBack,
            onFavoriteClick = { onAction(ScreenshotAction.ToggleFavorite) },
            onMoreClick = onOpenMore,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            ScreenshotDetailHeroImage(
                imageModel = imageModel,
                onFullscreenClick = onOpenFullscreen,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                enableSharedImageBounds = enableSharedImageBounds,
                fullscreenOwnsSharedImageRaster = fullscreenOwnsSharedImageRaster,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = ScreenshotTokens.HorizontalPadding,
                        top = ScreenshotTokens.ContentTopPadding,
                        end = ScreenshotTokens.HorizontalPadding,
                        bottom = ScreenshotDetailTokens.ContentBottomPadding,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            RecapCategoryTextChip(
                type = categoryType,
                textSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(ScreenshotDetailTokens.ScreenshotDetailSpacing))
            Text(
                text = analysis.title,
                modifier = Modifier.fillMaxWidth(),
                style = RecapHeading1,
                color = RecapGray900,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(ScreenshotDetailTokens.ScreenshotDetailSpacing))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(ScreenshotDetailTokens.SummaryCornerRadius))
                    .background(RecapGray50)
                    .padding(ScreenshotDetailTokens.SummaryPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = analysis.summary,
                    style = RecapBody1,
                    color = RecapGray500,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(ScreenshotDetailTokens.DividerVerticalSpacing))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = ScreenshotDetailTokens.DividerThickness,
                color = RecapGray100,
            )
            Spacer(modifier = Modifier.height(ScreenshotDetailTokens.DividerVerticalSpacing))
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ai_edit_16),
                        contentDescription = null,
                        tint = RecapGray200,
                        modifier = Modifier.size(ScreenshotDetailTokens.MetaIconSize),
                    )
                    Text(
                        text = stringResource(
                            R.string.screenshot_detail_organized_date_format,
                            formatDetailOrganizedDate(
                                card.analysisResult.organizedAt.toEpochMilli(),
                            ),
                        ),
                        modifier = Modifier.padding(
                            start = ScreenshotDetailTokens.MetaIconTextSpacing,
                        ),
                        style = RecapCaption1,
                        color = RecapGray300,
                    )
                }
                Spacer(modifier = Modifier.height(ScreenshotDetailTokens.MetaToBodySectionSpacing))
                Text(
                    text = bodyText,
                    modifier = Modifier.fillMaxWidth(),
                    style = RecapBody1,
                    color = RecapGray700,
                )
                content.actionErrorMessageResId?.let { errorResId ->
                    Spacer(modifier = Modifier.height(ScreenshotDetailTokens.SectionSpacing))
                    Text(
                        text = stringResource(errorResId),
                        style = RecapBody2,
                        color = RecapError,
                    )
                }
                Spacer(modifier = Modifier.height(ScreenshotDetailTokens.BodyToDisclaimerSpacing))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_error_circle_16),
                        contentDescription = null,
                        tint = RecapGray200,
                        modifier = Modifier.size(ScreenshotDetailTokens.MetaIconSize),
                    )
                    Text(
                        text = stringResource(R.string.screenshot_detail_ai_disclaimer),
                        modifier = Modifier.padding(
                            start = ScreenshotDetailTokens.MetaIconTextSpacing,
                        ),
                        style = RecapCaption2,
                        color = RecapGray300,
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun ScreenshotDetailTopBar(
    isFavorite: Boolean,
    favoriteEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val backInteractionSource = remember { MutableInteractionSource() }
    val favoriteInteractionSource = remember { MutableInteractionSource() }
    val moreInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(RecapBackground)
            .statusBarsPadding()
            .height(ScreenshotDetailTokens.TopBarHeight),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(ScreenshotDetailTokens.TopBarPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    ScreenshotDetailTokens.TopBarBackTitleSpacing,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left_24),
                    contentDescription = stringResource(
                        R.string.screenshot_detail_back_content_description,
                    ),
                    tint = RecapGray700,
                    modifier = Modifier
                        .size(ScreenshotDetailTokens.TopBarIconSize)
                        .clickable(
                            interactionSource = backInteractionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = onNavigateBack,
                        ),
                )
                Text(
                    text = stringResource(R.string.screenshot_detail_title),
                    style = RecapHeading2,
                    color = RecapGray900,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    ScreenshotDetailTokens.TopBarActionSpacing,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_filled_24),
                    contentDescription = stringResource(
                        if (isFavorite) {
                            R.string.screenshot_detail_favorite_selected_content_description
                        } else {
                            R.string.screenshot_detail_favorite_content_description
                        },
                    ),
                    tint = if (isFavorite) {
                        RecapBlue300
                    } else {
                        RecapGray200
                    },
                    modifier = Modifier
                        .size(ScreenshotDetailTokens.TopBarIconSize)
                        .toggleable(
                            value = isFavorite,
                            enabled = favoriteEnabled,
                            role = Role.Checkbox,
                            interactionSource = favoriteInteractionSource,
                            indication = null,
                            onValueChange = { onFavoriteClick() },
                        ),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_more_24),
                    contentDescription = stringResource(
                        R.string.screenshot_detail_more_content_description,
                    ),
                    tint = RecapGray900,
                    modifier = Modifier
                        .size(ScreenshotDetailTokens.TopBarIconSize)
                        .clickable(
                            interactionSource = moreInteractionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = onMoreClick,
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ScreenshotDetailHeroImage(
    imageModel: Any?,
    onFullscreenClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    enableSharedImageBounds: Boolean,
    fullscreenOwnsSharedImageRaster: Boolean,
) {
    var imageLoadFailed by remember(imageModel) { mutableStateOf(false) }
    val showPlaceholder = imageModel == null || imageLoadFailed
    val imageInteractionSource = remember { MutableInteractionSource() }
    val imageShape = RoundedCornerShape(ScreenshotSharedImageCornerRadius)
    val suppressSharedImageContent = shouldSuppressSharedImageContent(
        enableSharedImageBounds = enableSharedImageBounds,
        fullscreenOwnsSharedImageRaster = fullscreenOwnsSharedImageRaster,
        isSharedTransitionActive = sharedTransitionScope?.isTransitionActive == true,
    )
    val sharedBoundsModifier = if (showPlaceholder || !enableSharedImageBounds) {
        Modifier
    } else {
        Modifier.screenshotSharedImageBounds(
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // Shared bounds on the frame that owns the chip so BottomEnd tracks morph.
        Box(
            modifier = Modifier
                .padding(vertical = ScreenshotDetailTokens.HeroImageVerticalPadding)
                .fillMaxWidth(ScreenshotDetailTokens.HeroImageWidthFraction)
                .aspectRatio(ScreenshotDetailTokens.HeroImageAspectRatio)
                .then(sharedBoundsModifier),
        ) {
            if (!suppressSharedImageContent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(imageShape)
                        .border(
                            width = ScreenshotDetailTokens.HeroImageBorderWidth,
                            color = RecapGray100,
                            shape = imageShape,
                        )
                        .clickable(
                            enabled = !showPlaceholder,
                            interactionSource = imageInteractionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = onFullscreenClick,
                        ),
                ) {
                    if (showPlaceholder) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(RecapImagePlaceholderBackground),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.recap_placeholder_1),
                                contentDescription = stringResource(
                                    R.string.screenshot_image_placeholder_content_description,
                                ),
                                modifier = Modifier.size(width = 24.dp, height = 21.dp),
                            )
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
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .screenshotFullscreenChipTransition(
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .size(ScreenshotDetailTokens.FullscreenButtonSize)
                        .clickable(
                            enabled = !showPlaceholder,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onFullscreenClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    ScreenshotFullscreenChipButton(
                        enabled = !showPlaceholder,
                        contentDescription = stringResource(
                            R.string.screenshot_detail_fullscreen_content_description,
                        ),
                    )
                }
            }
        }
    }
}

/** Temporary outlined fullscreen chip. Kept private until Edit/Fullscreen refactor. */
@Composable
private fun ScreenshotFullscreenChipButton(
    contentDescription: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(ScreenshotDetailTokens.FullscreenChipCornerRadius)
    Box(
        modifier = modifier
            .size(ScreenshotDetailTokens.FullscreenChipSize)
            .background(color = White, shape = shape)
            .border(
                width = ScreenshotDetailTokens.FullscreenChipBorderWidth,
                color = RecapGray200,
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_fullscreen_24),
            contentDescription = contentDescription,
            tint = if (enabled) RecapGray900 else RecapGray900.copy(alpha = 0.38f),
            modifier = Modifier.size(ScreenshotDetailTokens.FullscreenChipIconSize),
        )
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
            .navigationBarsPadding()
            .padding(ScreenshotTokens.HorizontalPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = RecapBody1,
            color = RecapGray900,
        )
        actionErrorMessageResId?.let { errorResId ->
            Text(
                text = stringResource(errorResId),
                modifier = Modifier.padding(top = ScreenshotDetailTokens.MetaRowSpacing),
                style = RecapBody2,
                color = RecapError,
            )
        }
        Spacer(modifier = Modifier.height(ScreenshotDetailTokens.ErrorStateSpacing))
        RecapButton(
            text = stringResource(R.string.screenshot_detail_retry),
            onClick = onRetry,
            size = RecapButtonSize.Medium,
        )
        Spacer(modifier = Modifier.height(ScreenshotDetailTokens.MetaRowSpacing))
        RecapButton(
            text = stringResource(R.string.screenshot_action_close),
            onClick = onNavigateBack,
            size = RecapButtonSize.Medium,
            colors = RecapButtonDefaults.outlinedColors(),
        )
    }
}

internal fun previewScreenshotContent(
    title: String = "8월 1주차 weekly: AI 활용 내용 및 CMC부스",
    summary: String = "프로젝트 AI 활용 사례와 데모데이 CMC 부스 배치",
    body: String = """
        각 팀에서 기획, 개발, 운영 과정 중 AI를 어떻게 활용했는지 혹은 남은 기간 활용할지에 대한 고민 필요
        추후 부스 방문 기업 관계자를 위해 AI 활용 Workflow, 서비스 구성 과정, AI를 통한 효율 및 인사이트 공유 예정
        데모데이 CMC 공간은 금요일 미참여 및 토요일만 참여하는 특성상 동선에 구애받지 않는 곳으로 배치됨
    """.trimIndent(),
    contentType: ScreenshotContentType = ScreenshotContentType.RECORD,
    isFavorite: Boolean = false,
): ScreenshotUiState.Content {
    val analysis = ScreenshotAnalysisResult(
        captureId = 1L,
        title = title,
        summary = summary,
        typeCode = contentType,
        body = body,
        originalImageUrl = "mock://preview",
        isFavorite = isFavorite,
        organizedAt = Instant.ofEpochMilli(1_720_000_000_000L),
    )
    return ScreenshotUiState.Content(
        card = StoredScreenshotCard(
            analysisResult = analysis,
            imageRefs = ScreenshotCardImageRefs(),
            updatedAtMillis = 1_720_000_000_000L,
        ),
        editDraft = ScreenshotEditDraft(
            title = analysis.title,
            summary = analysis.summary,
            body = analysis.body,
            contentType = analysis.typeCode,
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

@Preview(
    name = "Screenshot Detail Long Body",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun ScreenshotDetailLongBodyPreview() {
    val longBody = buildString {
        repeat(50) { index ->
            appendLine(
                "${index + 1}. 본문 스크롤을 확인하기 위한 매우 긴 미리보기 문단입니다. " +
                        "내용이 계속 이어져도 레이아웃이 유지되어야 합니다.",
            )
        }
    }
    RECAPTheme(dynamicColor = false) {
        ScreenshotDetailScreen(
            uiState = previewScreenshotContent(body = longBody),
            onAction = {},
            onNavigateBack = {},
            onOpenEdit = {},
            onOpenFullscreen = {},
            onOpenMore = {},
        )
    }
}

private object ScreenshotDetailTokens {
    val TopBarHeight = 60.dp
    val TopBarPadding = 16.dp
    val TopBarIconSize = 24.dp
    val TopBarBackTitleSpacing = 13.dp
    val TopBarActionSpacing = 10.dp
    const val HeroImageWidthFraction = 0.35f
    const val HeroImageAspectRatio = 3f / 4f
    val HeroImageBorderWidth = 0.5.dp
    val HeroImageVerticalPadding = 17.dp
    val ContentBottomPadding = 32.dp
    val ScreenshotDetailSpacing = 14.dp
    val SummaryCornerRadius = 12.dp
    val SummaryPadding = 10.dp
    val DividerVerticalSpacing = 20.dp
    val DividerThickness = 1.dp
    val MetaIconSize = 16.dp
    val MetaIconTextSpacing = 5.dp
    val MetaToBodySectionSpacing = 10.dp
    val BodyToDisclaimerSpacing = 20.dp
    val SectionSpacing = 17.dp
    val MetaRowSpacing = 8.dp
    val FullscreenButtonSize = 41.dp
    val FullscreenChipSize = 21.dp
    val FullscreenChipIconSize = 13.5.dp
    val FullscreenChipCornerRadius = 2.dp
    val FullscreenChipBorderWidth = 0.5.dp
    val ErrorStateSpacing = 16.dp
}
