package com.chalkak.recap.core.design.component.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.component.chip.RecapCategoryChipDefaults
import com.chalkak.recap.core.design.component.chip.RecapCategoryTextChip
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScreenshotCard(
    thumbnailModel: Any?,
    title: String,
    description: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoryType: RecapCategoryType? = null,
    organizedAtMillis: Long? = null,
    thumbnailContentDescription: String? = null,
    horizontalContentPadding: Dp = ScreenshotCardTokens.ContainerHorizontalPadding,
    showFavoriteButton: Boolean = true,
    showBottomDivider: Boolean = true,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = RecapGray900,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = horizontalContentPadding,
                        vertical = ScreenshotCardTokens.ContainerVerticalPadding,
                    ),
                horizontalArrangement = Arrangement.spacedBy(
                    ScreenshotCardTokens.ContentSpacing,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (categoryType != null) {
                        RecapCategoryTextChip(
                            type = categoryType,
                            colors = RecapCategoryChipDefaults.colors(categoryType),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = RecapGray900,
                        maxLines = ScreenshotCardTokens.TitleMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelLarge,
                        color = RecapGray500,
                        maxLines = ScreenshotCardTokens.DescriptionMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (categoryType == null) {
                        val organizedDateLabel = remember(organizedAtMillis) {
                            organizedAtMillis?.let(::formatScreenshotOrganizedDate)
                        }
                        if (organizedDateLabel != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(
                                    R.string.collection_detail_organized_date,
                                    organizedDateLabel,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = RecapGray300,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                ScreenshotCardThumbnail(
                    thumbnailModel = thumbnailModel,
                    thumbnailContentDescription = thumbnailContentDescription,
                    isFavorite = isFavorite,
                    showFavoriteButton = showFavoriteButton,
                    onFavoriteClick = onFavoriteClick,
                )
            }
            if (showBottomDivider) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = horizontalContentPadding),
                    thickness = ScreenshotCardTokens.DividerThickness,
                    color = RecapGray100,
                )
            }
        }
    }
}

@Composable
private fun ScreenshotCardThumbnail(
    thumbnailModel: Any?,
    thumbnailContentDescription: String?,
    isFavorite: Boolean,
    showFavoriteButton: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rectProgress by animateFloatAsState(
        targetValue = if (showFavoriteButton) 0f else 1f,
        animationSpec = if (showFavoriteButton) {
            favoriteButtonExitTween()
        } else {
            favoriteButtonEnterTween()
        },
        label = "screenshotThumbnailShapeProgress",
    )
    val thumbnailShape = remember(rectProgress) {
        ScreenshotThumbnailShape(rectProgress)
    }

    Box(
        modifier = modifier.size(
            width = ScreenshotCardTokens.ThumbnailWidth,
            height = ScreenshotCardTokens.ThumbnailHeight,
        ),
    ) {
        AsyncImage(
            model = thumbnailModel,
            contentDescription = thumbnailContentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = ScreenshotCardTokens.ThumbnailBorderWidth,
                    color = RecapGray100,
                    shape = thumbnailShape,
                )
                .clip(thumbnailShape),
        )
        AnimatedVisibility(
            visible = showFavoriteButton,
            modifier = Modifier.offset(
                x = ScreenshotCardTokens.FavoriteIconOffsetX,
                y = ScreenshotCardTokens.FavoriteIconOffsetY,
            ),
            enter = fadeIn(animationSpec = favoriteButtonExitTween()) +
                    scaleIn(animationSpec = favoriteButtonExitTween()),
            exit = fadeOut(animationSpec = favoriteButtonEnterTween()) +
                    scaleOut(animationSpec = favoriteButtonEnterTween()),
        ) {
            ScreenshotCardStarButton(
                isFavorite = isFavorite,
                onClick = onFavoriteClick,
            )
        }
    }
}

@Composable
private fun ScreenshotCardStarButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val favoriteContentDescription = stringResource(
        if (isFavorite) {
            R.string.favorite_category_card_remove_favorite_content_description
        } else {
            R.string.favorite_category_card_add_favorite_content_description
        },
    )

    Icon(
        painter = painterResource(R.drawable.ic_star_16),
        contentDescription = favoriteContentDescription,
        modifier = modifier
            .size(ScreenshotCardTokens.FavoriteIconTouchSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(ScreenshotCardTokens.FavoriteIconPadding)
            .size(ScreenshotCardTokens.FavoriteIconSize),
        tint = if (isFavorite) {
            RecapBlue500
        } else {
            RecapGray200
        },
    )
}

/**
 * imagecard.svg viewBox(62 x 80) 기준 썸네일 clip [Shape].
 * [progress] 0 = 우상단 즐겨찾기 노치, 1 = 동일 코너 반경의 둥근 사각형.
 * 노치 제어점을 우상단으로 lerp해 선택 모드 진입 시 사각형으로 복구한다.
 */
private class ScreenshotThumbnailShape(
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val t = progress.coerceIn(0f, 1f)
        val sourcePath = Path().apply {
            // Top edge → notch/TR entry (imagecard.svg topology)
            moveTo(5.04688f, 0.25f)
            lineTo(lerp(31.7207f, 56.9531f, t), 0.25f)
            cubicTo(
                lerp(33.0136f, 58.246f, t),
                0.25f,
                lerp(34.2074f, 59.4398f, t),
                0.976281f,
                lerp(35.0869f, 60.3193f, t),
                2.19629f,
            )
            cubicTo(
                lerp(35.9662f, 61.1986f, t),
                3.41594f,
                lerp(36.5176f, 61.75f, t),
                5.11364f,
                lerp(36.5176f, 61.75f, t),
                7f,
            )
            // Notch column + inner corner collapse toward top-right
            lineTo(lerp(36.5176f, 61.75f, t), lerp(17f, 7f, t))
            cubicTo(
                lerp(36.5176f, 61.75f, t),
                lerp(18.9796f, 7f, t),
                lerp(37.0958f, 61.75f, t),
                lerp(20.7819f, 7f, t),
                lerp(38.043f, 61.75f, t),
                lerp(22.0957f, 7f, t),
            )
            cubicTo(
                lerp(38.9898f, 61.75f, t),
                lerp(23.4091f, 7f, t),
                lerp(40.3195f, 61.75f, t),
                lerp(24.2498f, 7f, t),
                lerp(41.8135f, 61.75f, t),
                lerp(24.25f, 7f, t),
            )
            lineTo(lerp(56.9531f, 61.75f, t), lerp(24.25f, 7f, t))
            cubicTo(
                lerp(58.246f, 61.75f, t),
                lerp(24.25f, 7f, t),
                lerp(59.4398f, 61.75f, t),
                lerp(24.9763f, 7f, t),
                lerp(60.3193f, 61.75f, t),
                lerp(26.1963f, 7f, t),
            )
            cubicTo(
                lerp(61.1986f, 61.75f, t),
                lerp(27.4159f, 7f, t),
                61.75f,
                lerp(29.1136f, 7f, t),
                61.75f,
                lerp(31f, 7f, t),
            )
            // Shared right / bottom / left edges
            lineTo(61.75f, 73f)
            cubicTo(61.75f, 74.8864f, 61.1986f, 76.5841f, 60.3193f, 77.8037f)
            cubicTo(59.4398f, 79.0237f, 58.246f, 79.75f, 56.9531f, 79.75f)
            lineTo(5.04688f, 79.75f)
            cubicTo(3.75398f, 79.75f, 2.5602f, 79.0237f, 1.68066f, 77.8037f)
            cubicTo(0.801379f, 76.5841f, 0.25f, 74.8864f, 0.25f, 73f)
            lineTo(0.25f, 7f)
            cubicTo(0.25f, 5.11361f, 0.801379f, 3.41594f, 1.68066f, 2.19629f)
            cubicTo(2.5602f, 0.976284f, 3.75398f, 0.25f, 5.04688f, 0.25f)
            close()
        }
        val scaleX = size.width / ScreenshotCardTokens.ThumbnailViewBoxWidth
        val scaleY = size.height / ScreenshotCardTokens.ThumbnailViewBoxHeight
        val scaledPath = Path().apply {
            addPath(sourcePath)
            transform(
                Matrix().apply {
                    scale(x = scaleX, y = scaleY)
                },
            )
        }
        return Outline.Generic(scaledPath)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScreenshotThumbnailShape) return false
        return progress == other.progress
    }

    override fun hashCode(): Int = progress.hashCode()
}

fun formatScreenshotOrganizedDate(
    organizedAtMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String {
    val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.getDefault())
    return Instant.ofEpochMilli(organizedAtMillis)
        .atZone(zoneId)
        .format(formatter)
}

private object ScreenshotCardTokens {
    val ContainerHorizontalPadding = 16.dp
    val ContainerVerticalPadding = 10.dp
    val ContentSpacing = 44.dp
    val ThumbnailWidth = 62.dp
    val ThumbnailHeight = 80.dp
    val ThumbnailBorderWidth = 0.5.dp
    val DividerThickness = 1.dp
    val FavoriteIconOffsetX = 35.dp
    val FavoriteIconOffsetY = (-2).dp
    val FavoriteIconTouchSize = 28.dp
    val FavoriteIconPadding = 6.dp
    val FavoriteIconSize = 16.dp
    const val ThumbnailViewBoxWidth = 62f
    const val ThumbnailViewBoxHeight = 80f
    const val TitleMaxLines = 1
    const val DescriptionMaxLines = 2

    // Matches CollectionSelectionTokens checkbox appear/disappear timing.
    const val FavoriteButtonEnterDurationMillis = 180
    const val FavoriteButtonExitDurationMillis = 150
}

private fun <T> favoriteButtonEnterTween() = tween<T>(
    durationMillis = ScreenshotCardTokens.FavoriteButtonEnterDurationMillis,
    easing = LinearOutSlowInEasing,
)

private fun <T> favoriteButtonExitTween() = tween<T>(
    durationMillis = ScreenshotCardTokens.FavoriteButtonExitDurationMillis,
    easing = FastOutLinearInEasing,
)

@Preview(name = "Screenshot Card", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotCardPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            categoryType = RecapCategoryType.InfoKnowledge,
            title = ScreenshotCardPreviewTitle,
            description = ScreenshotCardPreviewDescription,
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Screenshot Card uncategorized", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotCardUncategorizedPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            categoryType = null,
            organizedAtMillis = 1_719_446_400_000L,
            title = ScreenshotCardPreviewTitle,
            description = ScreenshotCardPreviewDescription,
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Screenshot Card favorited", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotCardFavoritedPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            categoryType = RecapCategoryType.InfoKnowledge,
            title = ScreenshotCardPreviewTitle,
            description = ScreenshotCardPreviewDescription,
            isFavorite = true,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Screenshot Card selection", showBackground = true, widthDp = 360)
@Composable
private fun ScreenshotCardSelectionPreview() {
    RECAPTheme(dynamicColor = false) {
        ScreenshotCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            categoryType = RecapCategoryType.InfoKnowledge,
            title = ScreenshotCardPreviewTitle,
            description = ScreenshotCardPreviewDescription,
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {},
            showFavoriteButton = false,
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val ScreenshotCardPreviewTitle = "파스타 레시피 저장"
private const val ScreenshotCardPreviewDescription =
    "한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약 한 줄 요약"
