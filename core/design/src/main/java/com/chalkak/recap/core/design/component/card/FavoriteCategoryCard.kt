package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun FavoriteCategoryCard(
    thumbnailModel: Any?,
    categoryLabel: String,
    title: String,
    description: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailContentDescription: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FavoriteCategoryCardTokens.ContainerCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        contentColor = RecapGray900,
    ) {
        Row(
            modifier = Modifier.padding(FavoriteCategoryCardTokens.ContainerPadding),
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
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(end = FavoriteCategoryCardTokens.FavoriteIconTouchSize),
                    verticalArrangement = Arrangement.spacedBy(FavoriteCategoryCardTokens.TextSpacing),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FavoriteCategoryBadge(label = categoryLabel)
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RecapGray900,
                        maxLines = FavoriteCategoryCardTokens.TitleMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelMedium,
                        color = RecapGray500,
                        maxLines = FavoriteCategoryCardTokens.DescriptionMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FavoriteCategoryStarButton(
                    isFavorite = isFavorite,
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }
    }
}

@Composable
private fun FavoriteCategoryBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = FavoriteCategoryCardTokens.BadgeBackgroundColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = FavoriteCategoryCardTokens.BadgeHorizontalPadding,
                vertical = FavoriteCategoryCardTokens.BadgeVerticalPadding,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = FavoriteCategoryCardTokens.BadgeTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
    val ContainerCornerRadius = 12.dp
    val ContainerPadding = 16.dp
    val ContentSpacing = 12.dp
    val ThumbnailSize = 72.dp
    val ThumbnailCornerRadius = 12.dp
    val TextSpacing = 4.dp
    val BadgeHorizontalPadding = 8.dp
    val BadgeVerticalPadding = 4.dp
    val FavoriteIconTouchSize = 32.dp
    val FavoriteIconPadding = 4.dp
    val FavoriteIconSize = 24.dp
    val BadgeBackgroundColor = RecapBlue50
    val BadgeTextColor = RecapBlue500
    const val TitleMaxLines = 1
    const val DescriptionMaxLines = 2
}

@Preview(name = "Favorite Category Card", showBackground = true, widthDp = 360)
@Composable
private fun FavoriteCategoryCardPreview() {
    RECAPTheme(dynamicColor = false) {
        FavoriteCategoryCard(
            thumbnailModel = R.drawable.bid_landscape_24px,
            categoryLabel = FavoriteCategoryCardPreviewCategoryLabel,
            title = FavoriteCategoryCardPreviewTitle,
            description = FavoriteCategoryCardPreviewDescription,
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {},
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
            categoryLabel = FavoriteCategoryCardPreviewCategoryLabel,
            title = FavoriteCategoryCardPreviewTitle,
            description = FavoriteCategoryCardPreviewDescription,
            isFavorite = true,
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val FavoriteCategoryCardPreviewCategoryLabel = "카테고리 01"
private const val FavoriteCategoryCardPreviewTitle = "무선 키보드 후보"
private const val FavoriteCategoryCardPreviewDescription = "가격과 배송 정보가 포함된 상품 캡처"
