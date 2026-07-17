package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapCaption1

@Composable
fun ShareFavoriteGuideCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(R.string.share_favorite_guide_card_title)
    val description = stringResource(R.string.share_favorite_guide_card_description)
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = "$title, $description"
            },
        shape = RoundedCornerShape(ShareFavoriteGuideCardTokens.CornerRadius),
        color = RecapGray50,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShareFavoriteGuideCardTokens.CardHeight),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(
                        start = ShareFavoriteGuideCardTokens.HorizontalPadding,
                        end = ShareFavoriteGuideCardTokens.IllustrationWidth +
                                ShareFavoriteGuideCardTokens.ContentToIllustrationSpacing,
                    ),
                verticalArrangement = Arrangement.spacedBy(
                    ShareFavoriteGuideCardTokens.TextSpacing,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        ShareFavoriteGuideCardTokens.TitleChevronSpacing,
                    ),
                ) {
                    Text(
                        text = title,
                        style = RecapBody1,
                        color = RecapGray900,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right_24),
                        contentDescription = null,
                        modifier = Modifier.size(ShareFavoriteGuideCardTokens.ChevronSize),
                        tint = RecapGray300,
                    )
                }
                Text(
                    text = description,
                    style = RecapCaption1,
                    color = RecapGray500,
                )
            }
            Image(
                painter = painterResource(R.drawable.illust_share_favorite_guide),
                contentDescription = stringResource(
                    R.string.share_favorite_guide_card_illustration_content_description,
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = ShareFavoriteGuideCardTokens.IllustrationEndPadding)
                    .size(
                        width = ShareFavoriteGuideCardTokens.IllustrationWidth,
                        height = ShareFavoriteGuideCardTokens.IllustrationHeight,
                    ),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private object ShareFavoriteGuideCardTokens {
    val CornerRadius = 10.dp
    val CardHeight = 115.dp
    val HorizontalPadding = 20.dp
    val TextSpacing = 14.dp
    val TitleChevronSpacing = 6.dp
    val ChevronSize = 16.dp
    val ContentToIllustrationSpacing = 32.dp
    val IllustrationEndPadding = 12.dp
    val IllustrationWidth = 125.dp
    val IllustrationHeight = 99.dp
}

@Preview(name = "Share Favorite Guide Card", showBackground = true, widthDp = 360)
@Composable
private fun ShareFavoriteGuideCardPreview() {
    RECAPTheme(dynamicColor = false) {
        ShareFavoriteGuideCard(
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
