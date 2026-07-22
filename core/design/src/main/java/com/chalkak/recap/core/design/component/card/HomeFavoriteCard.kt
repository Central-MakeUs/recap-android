package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption3
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.White

@Composable
fun HomeFavoriteCard(
    categoryType: RecapCategoryType,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryLabel = stringResource(categoryType.labelResId)
    val contentDescription = stringResource(
        R.string.home_favorite_card_content_description,
        categoryLabel,
        title,
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        shape = RoundedCornerShape(HomeFavoriteCardTokens.CornerRadius),
        color = RecapGray50,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(HomeFavoriteCardTokens.Padding),
            verticalArrangement = Arrangement.spacedBy(HomeFavoriteCardTokens.SectionSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    HomeFavoriteCardTokens.HeaderSpacing,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(HomeFavoriteCardTokens.IconContainerSize),
                    shape = RoundedCornerShape(HomeFavoriteCardTokens.IconContainerRadius),
                    color = White,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(HomeFavoriteCardTokens.IconContainerSize),
                    ) {
                        Icon(
                            painter = painterResource(categoryType.iconResId),
                            contentDescription = null,
                            tint = categoryType.borderColor,
                            modifier = Modifier.size(HomeFavoriteCardTokens.IconSize),
                        )
                    }
                }
                Text(
                    text = categoryLabel,
                    modifier = Modifier.weight(1f),
                    style = RecapCaption3,
                    color = RecapGray900,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right_24),
                    contentDescription = null,
                    tint = RecapGray200,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                style = RecapHeading3,
                color = RecapGray900,
                maxLines = HomeFavoriteCardTokens.TitleMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private object HomeFavoriteCardTokens {
    val CornerRadius = 16.dp
    val Padding = 16.dp
    val SectionSpacing = 12.dp
    val HeaderSpacing = 8.dp
    val IconContainerSize = 24.dp
    val IconContainerRadius = 10.dp
    val IconSize = 16.dp
    const val TitleMaxLines = 1
}

@Preview(name = "Home Favorite Card", showBackground = true, widthDp = 200)
@Composable
private fun HomeFavoriteCardPreview() {
    RECAPTheme(dynamicColor = false) {
        HomeFavoriteCard(
            categoryType = RecapCategoryType.RecordCapture,
            title = stringResource(R.string.home_favorite_card_preview_title),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Home Favorite Card Long Title", showBackground = true, widthDp = 200)
@Composable
private fun HomeFavoriteCardLongTitlePreview() {
    RECAPTheme(dynamicColor = false) {
        HomeFavoriteCard(
            categoryType = RecapCategoryType.ShoppingProduct,
            title = stringResource(R.string.home_favorite_card_preview_long_title),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
