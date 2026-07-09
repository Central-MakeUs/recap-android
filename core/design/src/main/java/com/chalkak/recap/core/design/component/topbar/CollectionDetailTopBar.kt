package com.chalkak.recap.core.design.component.topbar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900

@Composable
fun CollectionDetailTopBar(
    title: String,
    countText: String,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes leadingIconResId: Int? = null,
    leadingIconTint: Color = RecapBlue500,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CollectionDetailTopBarHeight)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CollectionDetailTopBarIconButton(
                    onClick = onBackClick,
                    iconResId = R.drawable.ic_chevron_left_24,
                    contentDescription = stringResource(
                        R.string.collection_back_content_description,
                    ),
                    tint = RecapGray900,
                )
                Row(
                    modifier = Modifier
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (leadingIconResId != null) {
                        Icon(
                            painter = painterResource(leadingIconResId),
                            contentDescription = null,
                            tint = leadingIconTint,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RecapGray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.labelLarge,
                        color = RecapGray500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                CollectionDetailTopBarIconButton(
                    onClick = onSearchClick,
                    iconResId = R.drawable.ic_search_24,
                    contentDescription = stringResource(
                        R.string.main_top_bar_search_content_description,
                    ),
                    tint = RecapGray900,
                )
            }
        }
    }
}

@Composable
private fun CollectionDetailTopBarIconButton(
    onClick: () -> Unit,
    @DrawableRes iconResId: Int,
    contentDescription: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}

private val CollectionDetailTopBarHeight = 56.dp

@Preview(name = "Collection Detail Top Bar", showBackground = true, widthDp = 360)
@Composable
private fun CollectionDetailTopBarPreview() {
    RECAPTheme(dynamicColor = false) {
        CollectionDetailTopBar(
            title = "쇼핑 · 상품",
            countText = "3 recaps",
            leadingIconResId = R.drawable.ic_cart_16,
            onBackClick = {},
            onSearchClick = {},
        )
    }
}
