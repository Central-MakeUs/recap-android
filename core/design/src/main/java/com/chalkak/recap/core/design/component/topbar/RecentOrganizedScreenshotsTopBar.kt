package com.chalkak.recap.core.design.component.topbar

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme

@Composable
fun RecentOrganizedScreenshotsTopBar(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                    .height(RecentOrganizedScreenshotsTopBarHeight)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                RecentOrganizedScreenshotsTopBarIconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_left_24),
                        contentDescription = stringResource(
                            R.string.collection_back_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
                RecentOrganizedScreenshotsTopBarIconButton(onClick = onSearchClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search_24),
                        contentDescription = stringResource(
                            R.string.main_top_bar_search_content_description,
                        ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentOrganizedScreenshotsTopBarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.size(36.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.background,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(36.dp),
        ) {
            content()
        }
    }
}

private val RecentOrganizedScreenshotsTopBarHeight = 64.dp

@Preview(name = "Recent Organized Screenshots Top Bar", showBackground = true, widthDp = 360)
@Composable
private fun RecentOrganizedScreenshotsTopBarPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentOrganizedScreenshotsTopBar(
            onBackClick = {},
            onSearchClick = {},
        )
    }
}
