package com.chalkak.recap.feature.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.chalkak.recap.core.design.theme.RecapBlue50
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
fun CollectionDetailScreen(
    detail: CollectionDetailUiModel,
    onBackClick: () -> Unit,
    onSortSelected: (CollectionListSort) -> Unit,
    onFavoriteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CollectionDetailHeader(
            title = stringResource(detail.titleResId),
            countText = stringResource(R.string.collection_detail_capture_count, detail.count),
            selectedSort = detail.sort,
            onBackClick = onBackClick,
            onSortSelected = onSortSelected,
        )

        if (detail.cards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(detail.emptyMessageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RecapGray500,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = detail.cards,
                    key = { card -> card.imageId },
                ) { card ->
                    CollectionDetailCardRow(
                        card = card,
                        onFavoriteClick = { onFavoriteClick(card.imageId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionDetailHeader(
    title: String,
    countText: String,
    selectedSort: CollectionListSort,
    onBackClick: () -> Unit,
    onSortSelected: (CollectionListSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_left_24),
            contentDescription = stringResource(R.string.collection_back_content_description),
            modifier = Modifier
                .size(32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onBackClick,
                )
                .padding(4.dp),
            tint = RecapGray900,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = countText,
                style = MaterialTheme.typography.labelMedium,
                color = RecapGray500,
            )
        }
        CollectionSortDropdown(
            selectedSort = selectedSort,
            onSortSelected = onSortSelected,
        )
    }
}

@Composable
private fun CollectionSortDropdown(
    selectedSort: CollectionListSort,
    onSortSelected: (CollectionListSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selectedSort) {
        CollectionListSort.Latest -> stringResource(R.string.collection_sort_latest)
        CollectionListSort.Name -> stringResource(R.string.collection_sort_name)
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = RecapGray500,
            )
            Icon(
                painter = painterResource(R.drawable.ic_dropdown_16),
                contentDescription = null,
                tint = RecapGray300,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.collection_sort_latest)) },
                onClick = {
                    expanded = false
                    onSortSelected(CollectionListSort.Latest)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.collection_sort_name)) },
                onClick = {
                    expanded = false
                    onSortSelected(CollectionListSort.Name)
                },
            )
        }
    }
}

@Composable
private fun CollectionDetailCardRow(
    card: CollectionCardItemUiModel,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RecapGray100),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CollectionThumbnail(
                model = card.thumbnailModel,
                modifier = Modifier.size(72.dp),
            )
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(end = 32.dp),
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
                        style = MaterialTheme.typography.labelMedium,
                        color = RecapGray500,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = RecapBlue50,
                        ) {
                            Text(
                                text = stringResource(card.contentTypeLabelResId),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = RecapBlue500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = formatCreatedDate(card.createdAtMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = RecapGray300,
                        )
                    }
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
                        .align(Alignment.TopEnd)
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
    }
}

private fun formatCreatedDate(createdAtMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.getDefault())
    return Instant.ofEpochMilli(createdAtMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

@Preview(name = "Collection Detail Populated", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionDetailPopulatedPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = CollectionDetailUiModel(
                titleResId = R.string.collection_content_type_shopping_product,
                count = 3,
                sort = CollectionListSort.Latest,
                cards = listOf(
                    CollectionCardItemUiModel(
                        imageId = "1",
                        title = "택배 반품 절차 정리",
                        summary = "배송 지연 시 반품 신청 방법 요약",
                        contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                        createdAtMillis = 1_718_208_000_000L,
                        isFavorite = true,
                        thumbnailModel = null,
                    ),
                    CollectionCardItemUiModel(
                        imageId = "2",
                        title = "노트북 가격 비교",
                        summary = "쿠팡 · 컴퓨존 견적 캡처 비교",
                        contentTypeLabelResId = R.string.collection_content_type_shopping_product,
                        createdAtMillis = 1_717_862_400_000L,
                        isFavorite = false,
                        thumbnailModel = null,
                    ),
                ),
                emptyMessageResId = R.string.collection_detail_empty,
            ),
            onBackClick = {},
            onSortSelected = {},
            onFavoriteClick = {},
        )
    }
}

@Preview(name = "Collection Detail Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CollectionDetailEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailScreen(
            detail = CollectionDetailUiModel(
                titleResId = R.string.collection_favorites_detail_title,
                count = 0,
                sort = CollectionListSort.Latest,
                cards = emptyList(),
                emptyMessageResId = R.string.collection_favorites_empty,
            ),
            onBackClick = {},
            onSortSelected = {},
            onFavoriteClick = {},
        )
    }
}
