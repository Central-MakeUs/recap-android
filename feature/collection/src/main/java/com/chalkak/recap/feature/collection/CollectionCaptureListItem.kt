package com.chalkak.recap.feature.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CollectionCaptureListItem(
    card: CollectionCardItemUiModel,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = CollectionCaptureListItemTokens.VerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(CollectionCaptureListItemTokens.ContentSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CollectionThumbnail(
            model = card.thumbnailModel,
            modifier = Modifier.size(CollectionCaptureListItemTokens.ThumbnailSize),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = card.summary,
                style = MaterialTheme.typography.labelLarge,
                color = RecapGray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    R.string.collection_detail_organized_date,
                    formatCollectionOrganizedDate(card.createdAtMillis),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = RecapGray300,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_star_24),
            contentDescription = stringResource(
                if (card.isFavorite) {
                    R.string.favorite_category_card_remove_favorite_content_description
                } else {
                    R.string.favorite_category_card_add_favorite_content_description
                },
            ),
            modifier = Modifier
                .size(32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onFavoriteClick,
                )
                .padding(4.dp)
                .size(24.dp),
            tint = if (card.isFavorite) RecapBlue500 else RecapGray200,
        )
    }
}

internal fun formatCollectionOrganizedDate(createdAtMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.getDefault())
    return Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private object CollectionCaptureListItemTokens {
    val VerticalPadding = 14.dp
    val ContentSpacing = 14.dp
    val ThumbnailSize = 72.dp
}

@Preview(name = "Collection Capture List Item", showBackground = true, widthDp = 360)
@Composable
private fun CollectionCaptureListItemPreview() {
    RECAPTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            CollectionCaptureListItem(
                card = CollectionCardItemUiModel(
                    imageId = "1",
                    title = "여름 원피스 주문 내역",
                    summary = "가격과 배송 정보가 포함된 상품 캡처",
                    contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                    createdAtMillis = 1_719_446_400_000L,
                    isFavorite = true,
                    thumbnailModel = null,
                ),
                onClick = {},
                onFavoriteClick = {},
            )
            CollectionCaptureListItem(
                card = CollectionCardItemUiModel(
                    imageId = "2",
                    title = "택배 반품 절차",
                    summary = "반품 신청 전 확인해야 할 체크리스트",
                    contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                    createdAtMillis = 1_718_208_000_000L,
                    isFavorite = false,
                    thumbnailModel = null,
                ),
                onClick = {},
                onFavoriteClick = {},
            )
        }
    }
}
