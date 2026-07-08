package com.chalkak.recap.feature.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
internal fun CollectionThumbnailStack(
    thumbnailModels: List<Any?>,
    modifier: Modifier = Modifier,
    showFavoriteBadge: Boolean = false,
) {
    Box(modifier = modifier.size(CollectionThumbnailStackTokens.ContainerSize)) {
        thumbnailModels.take(3).forEachIndexed { index, model ->
            CollectionThumbnail(
                model = model,
                modifier = Modifier
                    .size(CollectionThumbnailStackTokens.ThumbnailSize)
                    .offset(
                        x = (index * CollectionThumbnailStackTokens.OffsetStep).dp,
                        y = (index * CollectionThumbnailStackTokens.OffsetStep).dp,
                    ),
            )
        }
        if (showFavoriteBadge && thumbnailModels.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(CollectionThumbnailStackTokens.BadgeSize),
                shape = RoundedCornerShape(8.dp),
                color = RecapBlue50,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_24),
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                    tint = RecapBlue500,
                )
            }
        }
    }
}

@Composable
internal fun CollectionThumbnail(
    model: Any?,
    modifier: Modifier = Modifier,
) {
    if (model == null) {
        Surface(
            modifier = modifier.clip(RoundedCornerShape(CollectionThumbnailStackTokens.ThumbnailCornerRadius)),
            color = RecapGray100,
        ) {}
        return
    }

    AsyncImage(
        model = model,
        contentDescription = stringResource(R.string.collection_thumbnail_content_description),
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(CollectionThumbnailStackTokens.ThumbnailCornerRadius)),
    )
}

@Composable
internal fun formatCollectionTypeExampleText(
    exampleTitles: List<String>,
    additionalExampleCount: Int,
): String? {
    if (exampleTitles.isEmpty()) {
        return null
    }
    val joinedTitles = exampleTitles.take(2).joinToString(" · ")
    return if (additionalExampleCount > 0) {
        stringResource(R.string.collection_type_examples_more, joinedTitles, additionalExampleCount)
    } else {
        joinedTitles
    }
}

@Composable
internal fun CollectionOverviewCard(
    thumbnailModels: List<Any?>,
    title: String,
    subtitle: String,
    exampleTitles: List<String>,
    additionalExampleCount: Int,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    showFavoriteBadge: Boolean = false,
) {
    val exampleText = formatCollectionTypeExampleText(
        exampleTitles = exampleTitles,
        additionalExampleCount = additionalExampleCount,
    )
    val shape = RoundedCornerShape(CollectionOverviewCardTokens.CornerRadius)
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CollectionOverviewCardTokens.Padding),
            horizontalArrangement = Arrangement.spacedBy(CollectionOverviewCardTokens.ContentSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CollectionThumbnailStack(
                thumbnailModels = thumbnailModels,
                showFavoriteBadge = showFavoriteBadge,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RecapGray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = RecapGray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!exampleText.isNullOrBlank()) {
                    Text(
                        text = exampleText,
                        style = MaterialTheme.typography.labelMedium,
                        color = RecapGray300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (onClick != null) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right_24),
                    contentDescription = null,
                    tint = RecapGray300,
                )
            }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, RecapGray100),
            content = content,
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, RecapGray100),
            content = content,
        )
    }
}

@Composable
internal fun CollectionThinMessageCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CollectionOverviewCardTokens.CornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RecapGray100),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(
                horizontal = CollectionOverviewCardTokens.Padding,
                vertical = 14.dp,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = RecapGray500,
        )
    }
}

private object CollectionThumbnailStackTokens {
    val ContainerSize = 56.dp
    val ThumbnailSize = 44.dp
    val ThumbnailCornerRadius = 10.dp
    val OffsetStep = 4
    val BadgeSize = 24.dp
}

private object CollectionOverviewCardTokens {
    val CornerRadius = 16.dp
    val Padding = 16.dp
    val ContentSpacing = 12.dp
}

@Preview(name = "Collection Thumbnail Stack", showBackground = true)
@Composable
private fun CollectionThumbnailStackPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionThumbnailStack(
            thumbnailModels = listOf(null, null, null),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Collection Thumbnail Stack Favorite Badge", showBackground = true)
@Composable
private fun CollectionThumbnailStackFavoriteBadgePreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionThumbnailStack(
            thumbnailModels = listOf(null, null, null),
            showFavoriteBadge = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Collection Overview Card Type", showBackground = true, widthDp = 360)
@Composable
private fun CollectionOverviewCardTypePreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionOverviewCard(
            thumbnailModels = listOf(null, null, null),
            title = stringResource(R.string.collection_content_type_shopping_product),
            subtitle = stringResource(R.string.collection_type_count, 12),
            exampleTitles = listOf("택배 반품 절차", "노트북 가격 비교"),
            additionalExampleCount = 0,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Collection Overview Card Favorite", showBackground = true, widthDp = 360)
@Composable
private fun CollectionOverviewCardFavoritePreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionOverviewCard(
            thumbnailModels = listOf(null, null, null),
            title = stringResource(R.string.collection_favorites_section_title),
            subtitle = stringResource(R.string.collection_favorites_capture_count, 4),
            exampleTitles = emptyList(),
            additionalExampleCount = 0,
            onClick = {},
            showFavoriteBadge = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Collection Overview Card More Examples", showBackground = true, widthDp = 360)
@Composable
private fun CollectionOverviewCardMoreExamplesPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionOverviewCard(
            thumbnailModels = listOf(null),
            title = stringResource(R.string.collection_content_type_other),
            subtitle = stringResource(R.string.collection_type_count, 3),
            exampleTitles = listOf("이사 체크리스트"),
            additionalExampleCount = 2,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Collection Thin Message Card", showBackground = true, widthDp = 360)
@Composable
private fun CollectionThinMessageCardPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionThinMessageCard(
            message = stringResource(R.string.collection_favorites_empty),
            modifier = Modifier.padding(16.dp),
        )
    }
}
