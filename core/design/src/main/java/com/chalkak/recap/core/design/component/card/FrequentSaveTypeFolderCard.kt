package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun FrequentSaveTypeFolderCard(
    categoryLabel: String,
    recapCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(FrequentSaveTypeFolderCardTokens.CardWidth),
        shape = RoundedCornerShape(FrequentSaveTypeFolderCardTokens.ContainerCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        contentColor = RecapGray900,
    ) {
        Column(
            modifier = Modifier.padding(FrequentSaveTypeFolderCardTokens.ContainerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FrequentSaveTypeFolderCardTokens.ContentSpacing),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_folder_type_48),
                contentDescription = null,
                modifier = Modifier.size(FrequentSaveTypeFolderCardTokens.FolderIconSize),
                tint = androidx.compose.ui.graphics.Color.Unspecified,
            )
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RecapGray900,
                maxLines = FrequentSaveTypeFolderCardTokens.CategoryMaxLines,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.frequent_save_type_folder_card_recap_count, recapCount),
                style = MaterialTheme.typography.labelMedium,
                color = RecapGray500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private object FrequentSaveTypeFolderCardTokens {
    val CardWidth = 72.dp
    val ContainerCornerRadius = 12.dp
    val ContainerPadding = 4.dp
    val ContentSpacing = 6.dp
    val FolderIconSize = 48.dp
    const val CategoryMaxLines = 2
}

@Preview(name = "Frequent Save Type Folder Card", showBackground = true)
@Composable
private fun FrequentSaveTypeFolderCardPreview() {
    RECAPTheme(dynamicColor = false) {
        FrequentSaveTypeFolderCard(
            categoryLabel = FrequentSaveTypeFolderCardPreviewCategoryLabel,
            recapCount = FrequentSaveTypeFolderCardPreviewRecapCount,
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}

private const val FrequentSaveTypeFolderCardPreviewCategoryLabel = "쇼핑 · 상품"
private const val FrequentSaveTypeFolderCardPreviewRecapCount = 12
