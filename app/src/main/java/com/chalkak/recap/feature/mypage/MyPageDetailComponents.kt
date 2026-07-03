package com.chalkak.recap.feature.mypage

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.RecapTopBar
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300
import com.chalkak.recap.core.design.theme.RecapGray50

@Composable
internal fun MyPageDetailScreenScaffold(
    @StringRes titleResId: Int,
    onBackClick: () -> Unit,
    bottomContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapGray50,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(titleResId),
                onBackClick = onBackClick,
                backButtonContentDescription = stringResource(
                    R.string.my_page_back_content_description,
                ),
                containerColor = RecapGray50,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MyPageDetailTokens.HorizontalPadding)
                    .padding(top = MyPageDetailTokens.ContentTopPadding),
                verticalArrangement = Arrangement.spacedBy(MyPageDetailTokens.CardSpacing),
                content = content,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MyPageDetailTokens.HorizontalPadding)
                    .padding(bottom = MyPageDetailTokens.BottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MyPageDetailTokens.BottomSpacing),
                content = bottomContent,
            )
        }
    }
}

@Composable
internal fun MyPageDocumentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun MyPageGuideCard(
    icon: ImageVector,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconContainerColor: Color = RecapGray100,
) {
    MyPageInfoCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MyPageDetailTokens.CardContentSpacing),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier.size(MyPageDetailTokens.IconContainerSize),
                shape = RoundedCornerShape(MyPageDetailTokens.IconContainerRadius),
                color = iconContainerColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(MyPageDetailTokens.IconSize),
                        tint = iconTint,
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(MyPageDetailTokens.TextSpacing),
            ) {
                MyPageInfoTitle(text = stringResource(titleResId))
                MyPageInfoDescription(text = stringResource(descriptionResId))
            }
        }
    }
}

@Composable
internal fun MyPageServiceSummaryCard(
    modifier: Modifier = Modifier,
) {
    MyPageInfoCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.my_page_service_info_app_mark),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_brand_mark_name),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = stringResource(R.string.my_page_service_info_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.my_page_service_info_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = RecapGray300,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
internal fun MyPageServiceMenuGroup(
    items: List<MyPageServiceMenuItemData>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MyPageDetailTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, RecapGray100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                MyPageServiceMenuItem(item = item)
                if (index != items.lastIndex) {
                    HorizontalDivider(
                        color = RecapGray100,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MyPageDataCard(
    @StringRes labelResId: Int,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    MyPageInfoCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MyPageDetailTokens.TextSpacing),
        ) {
            Text(
                text = stringResource(labelResId),
                style = MaterialTheme.typography.labelMedium,
                color = RecapGray300,
                fontWeight = FontWeight.Bold,
            )
            MyPageInfoTitle(text = stringResource(titleResId))
            MyPageInfoDescription(text = stringResource(descriptionResId))
            if (action != null) {
                Spacer(modifier = Modifier.height(MyPageDetailTokens.ActionTopSpacing))
                action()
            }
        }
    }
}

@Composable
internal fun MyPageInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MyPageDetailTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, RecapGray100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier.padding(MyPageDetailTokens.CardPadding),
        ) {
            content()
        }
    }
}

@Composable
internal fun MyPageInfoTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
internal fun MyPageInfoDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun MyPageServiceMenuItem(
    item: MyPageServiceMenuItemData,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = item.onClick,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MyPageDetailTokens.ServiceMenuItemHorizontalPadding,
                vertical = MyPageDetailTokens.ServiceMenuItemVerticalPadding,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MyPageInfoTitle(text = stringResource(item.titleResId))
                MyPageInfoDescription(text = stringResource(item.descriptionResId))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

internal object MyPageDetailTokens {
    val HorizontalPadding = 16.dp
    val ContentTopPadding = 16.dp
    val BottomPadding = 36.dp
    val BottomSpacing = 14.dp
    val CardSpacing = 12.dp
    val CardRadius = 14.dp
    val CardPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp)
    val CardContentSpacing = 14.dp
    val TextSpacing = 4.dp
    val IconContainerSize = 44.dp
    val IconContainerRadius = 10.dp
    val IconSize = 22.dp
    val ActionTopSpacing = 8.dp
    val ServiceMenuItemHorizontalPadding = 16.dp
    val ServiceMenuItemVerticalPadding = 14.dp
    val WarningContainerColor = Color(0xFFFFF3D8)
    val WarningIconColor = Color(0xFFE8A21A)
}

internal data class MyPageServiceMenuItemData(
    @get:StringRes val titleResId: Int,
    @get:StringRes val descriptionResId: Int,
    val onClick: () -> Unit,
)
