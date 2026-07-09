package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.chip.RecapCategoryChip
import com.chalkak.recap.core.design.component.chip.RecapCategoryChipType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import java.util.concurrent.TimeUnit

@Composable
fun FavoriteCategoryCard(
    thumbnailModel: Any?,
    categoryType: RecapCategoryChipType,
    title: String,
    description: String,
    organizedAtMillis: Long,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    nowMillis: Long = System.currentTimeMillis(),
    thumbnailContentDescription: String? = null,
) {
    val relativeTimeLabel = remember(organizedAtMillis, nowMillis) {
        OrganizedRelativeTimeFormatter.label(
            organizedAtMillis = organizedAtMillis,
            nowMillis = nowMillis,
        )
    } ?: return

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = RecapGray900,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = FavoriteCategoryCardTokens.ContainerHorizontalPadding,
                vertical = FavoriteCategoryCardTokens.ContainerVerticalPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(FavoriteCategoryCardTokens.ContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = thumbnailModel,
                contentDescription = thumbnailContentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(FavoriteCategoryCardTokens.ThumbnailSize)
                    .clip(RoundedCornerShape(FavoriteCategoryCardTokens.ThumbnailCornerRadius)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FavoriteCategoryCardTokens.TextSpacing),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RecapCategoryChip(
                        type = categoryType,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = FavoriteCategoryCardTokens.TrailingContentSpacing),
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(
                            FavoriteCategoryCardTokens.TrailingContentSpacing,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = relativeTimeLabel.toDisplayText(),
                            style = MaterialTheme.typography.labelSmall,
                            color = RecapGray300,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        FavoriteCategoryStarButton(
                            isFavorite = isFavorite,
                            onClick = onFavoriteClick,
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = RecapGray900,
                    maxLines = FavoriteCategoryCardTokens.TitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelLarge,
                    color = RecapGray500,
                    maxLines = FavoriteCategoryCardTokens.DescriptionMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun OrganizedRelativeTimeLabel.toDisplayText(): String {
    return when (this) {
        OrganizedRelativeTimeLabel.JustNow -> {
            stringResource(R.string.organized_relative_time_just_now)
        }
        is OrganizedRelativeTimeLabel.MinutesAgo -> {
            stringResource(R.string.organized_relative_time_minutes_ago, minutes)
        }
        is OrganizedRelativeTimeLabel.HoursAgo -> {
            stringResource(R.string.organized_relative_time_hours_ago, hours)
        }
        OrganizedRelativeTimeLabel.Yesterday -> {
            stringResource(R.string.organized_relative_time_yesterday)
        }
        is OrganizedRelativeTimeLabel.DaysAgo -> {
            stringResource(R.string.organized_relative_time_days_ago, days)
        }
    }
}

@Composable
private fun FavoriteCategoryStarButton(
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
        painter = painterResource(R.drawable.ic_star_24),
        contentDescription = favoriteContentDescription,
        modifier = modifier
            .size(FavoriteCategoryCardTokens.FavoriteIconTouchSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(FavoriteCategoryCardTokens.FavoriteIconPadding)
            .size(FavoriteCategoryCardTokens.FavoriteIconSize),
        tint = if (isFavorite) {
            RecapBlue500
        } else {
            RecapGray200
        },
    )
}

private object FavoriteCategoryCardTokens {
    val ContainerHorizontalPadding = 16.dp
    val ContainerVerticalPadding = 10.dp
    val ContentSpacing = 15.dp
    val ThumbnailSize = 68.dp
    val ThumbnailCornerRadius = 12.dp
    val TextSpacing = 4.dp
    val TrailingContentSpacing = 4.dp
    val FavoriteIconTouchSize = 32.dp
    val FavoriteIconPadding = 4.dp
    val FavoriteIconSize = 24.dp
    const val TitleMaxLines = 1
    const val DescriptionMaxLines = 2
}

@Preview(name = "Favorite Category Card", showBackground = true, widthDp = 360)
@Composable
private fun FavoriteCategoryCardPreview() {
    RECAPTheme(dynamicColor = false) {
        FavoriteCategoryCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            categoryType = RecapCategoryChipType.ShoppingProduct,
            title = FavoriteCategoryCardPreviewTitle,
            description = FavoriteCategoryCardPreviewDescription,
            organizedAtMillis = FavoriteCategoryCardPreviewNowMillis -
                TimeUnit.HOURS.toMillis(1),
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {},
            nowMillis = FavoriteCategoryCardPreviewNowMillis,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(name = "Favorite Category Card favorited", showBackground = false, widthDp = 360)
@Composable
private fun FavoriteCategoryCardFavoritedPreview() {
    RECAPTheme(dynamicColor = false) {
        FavoriteCategoryCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            categoryType = RecapCategoryChipType.ShoppingProduct,
            title = FavoriteCategoryCardPreviewTitle,
            description = FavoriteCategoryCardPreviewDescription,
            organizedAtMillis = FavoriteCategoryCardPreviewNowMillis -
                TimeUnit.MINUTES.toMillis(30),
            isFavorite = true,
            onClick = {},
            onFavoriteClick = {},
            nowMillis = FavoriteCategoryCardPreviewNowMillis,
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val FavoriteCategoryCardPreviewTitle = "택배 반품 절차 정리"
private const val FavoriteCategoryCardPreviewDescription = "한 줄 요약"
private const val FavoriteCategoryCardPreviewNowMillis = 1_720_000_000_000L
