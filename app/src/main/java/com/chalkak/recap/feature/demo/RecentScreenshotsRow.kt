package com.chalkak.recap.feature.demo

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import coil3.compose.AsyncImage

@Composable
fun RecentScreenshotsRow(
    imageUris: List<Uri>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "최근 스크린샷",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "DCIM/Screenshots · 최근 5개",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (imageUris.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "표시할 이미지가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    imageUris.forEachIndexed { index, uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "최근 스크린샷 ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 104.dp, height = 148.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Recent Screenshots - Empty")
@Preview(name = "Recent Screenshots - Empty Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecentScreenshotsRowEmptyPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentScreenshotsRow(
            imageUris = emptyList(),
        )
    }
}

@Preview(name = "Recent Screenshots - Populated")
@Composable
private fun RecentScreenshotsRowPopulatedPreview() {
    RECAPTheme(dynamicColor = false) {
        RecentScreenshotsRow(
            imageUris = listOf(
                Uri.parse("content://com.chalkak.recap.preview/screenshot/1"),
                Uri.parse("content://com.chalkak.recap.preview/screenshot/2"),
                Uri.parse("content://com.chalkak.recap.preview/screenshot/3"),
                Uri.parse("content://com.chalkak.recap.preview/screenshot/4"),
            ),
        )
    }
}
