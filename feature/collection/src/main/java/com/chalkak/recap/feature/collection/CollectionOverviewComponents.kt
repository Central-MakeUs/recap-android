package com.chalkak.recap.feature.collection

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapBlue500
import com.chalkak.recap.core.design.theme.RecapGray200
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray700
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.core.design.theme.RecapTypography.RecapHeading3
import com.chalkak.recap.core.design.theme.White

@Composable
fun CollectionFavoritesEntryCard(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = stringResource(
        R.string.collection_favorites_entry_content_description,
        count,
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        shape = RoundedCornerShape(CollectionFavoritesEntryCardTokens.CornerRadius),
        color = RecapBlue50,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CollectionFavoritesEntryCardTokens.Padding),
            horizontalArrangement = Arrangement.spacedBy(
                CollectionFavoritesEntryCardTokens.ContentSpacing,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .size(CollectionFavoritesEntryCardTokens.IconContainerSize)
                    .align(alignment = Alignment.Top),
                shape = RoundedCornerShape(CollectionFavoritesEntryCardTokens.IconContainerRadius),
                color = White,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_24),
                    contentDescription = null,
                    modifier = Modifier.padding(CollectionFavoritesEntryCardTokens.IconPadding),
                    tint = RecapBlue500,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.collection_favorites_section_title),
                    style = RecapHeading3,
                    color = RecapGray900,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = RecapGray700,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        ) {
                            append(count.toString())
                        }
                        append(" ")
                        withStyle(SpanStyle(color = RecapGray500)) {
                            append(
                                pluralStringResource(
                                    R.plurals.recap_haze_folder_card_recap_label,
                                    count,
                                ),
                            )
                        }
                    },
                    style = RecapBody2,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right_24),
                contentDescription = null,
                tint = RecapGray200,
                modifier = Modifier.align(alignment = Alignment.Top)
            )
        }
    }
}

private object CollectionFavoritesEntryCardTokens {
    val CornerRadius = 16.dp
    val Padding = 16.dp
    val ContentSpacing = 12.dp
    val IconContainerSize = 30.dp
    val IconContainerRadius = 12.dp
    val IconPadding = 4.dp
}

@Preview(name = "Favorites Entry Card Zero", showBackground = true, widthDp = 360)
@Composable
private fun CollectionFavoritesEntryCardZeroPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionFavoritesEntryCard(
            count = 0,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Favorites Entry Card Populated", showBackground = true, widthDp = 360)
@Composable
private fun CollectionFavoritesEntryCardPopulatedPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionFavoritesEntryCard(
            count = 12,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
