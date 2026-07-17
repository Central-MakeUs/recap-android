package com.chalkak.recap.feature.settings

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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray300

@Composable
internal fun SettingsDetailScreenScaffold(
    @StringRes titleResId: Int,
    onBackClick: () -> Unit,
    bottomContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(titleResId),
                onBackClick = onBackClick,
                backButtonContentDescription = stringResource(
                    R.string.settings_back_content_description,
                ),
                containerColor = MaterialTheme.colorScheme.background,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = SettingsDetailTokens.HorizontalPadding)
                    .padding(top = SettingsDetailTokens.ContentTopPadding),
                verticalArrangement = Arrangement.spacedBy(SettingsDetailTokens.CardSpacing),
                content = content,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SettingsDetailTokens.HorizontalPadding)
                    .padding(bottom = SettingsDetailTokens.BottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SettingsDetailTokens.BottomSpacing),
                content = bottomContent,
            )
        }
    }
}

@Composable
internal fun SettingsDocumentButton(
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
internal fun SettingsGuideCard(
    icon: ImageVector,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconContainerColor: Color = RecapGray100,
) {
    SettingsInfoCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SettingsDetailTokens.CardContentSpacing),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier.size(SettingsDetailTokens.IconContainerSize),
                shape = RoundedCornerShape(SettingsDetailTokens.IconContainerRadius),
                color = iconContainerColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(SettingsDetailTokens.IconSize),
                        tint = iconTint,
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(SettingsDetailTokens.TextSpacing),
            ) {
                SettingsInfoTitle(text = stringResource(titleResId))
                SettingsInfoDescription(text = stringResource(descriptionResId))
            }
        }
    }
}

@Composable
internal fun SettingsDataCard(
    @StringRes labelResId: Int,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    SettingsInfoCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SettingsDetailTokens.TextSpacing),
        ) {
            Text(
                text = stringResource(labelResId),
                style = MaterialTheme.typography.labelMedium,
                color = RecapGray300,
                fontWeight = FontWeight.Bold,
            )
            SettingsInfoTitle(text = stringResource(titleResId))
            SettingsInfoDescription(text = stringResource(descriptionResId))
            if (action != null) {
                Spacer(modifier = Modifier.height(SettingsDetailTokens.ActionTopSpacing))
                action()
            }
        }
    }
}

@Composable
internal fun SettingsInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsDetailTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, RecapGray100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier.padding(SettingsDetailTokens.CardPadding),
        ) {
            content()
        }
    }
}

@Composable
internal fun SettingsInfoTitle(
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
internal fun SettingsInfoDescription(
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

internal object SettingsDetailTokens {
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
    val WarningContainerColor = Color(0xFFFFF3D8)
    val WarningIconColor = Color(0xFFE8A21A)
}

@Preview(name = "Settings Detail Scaffold", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SettingsDetailScreenScaffoldPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsDetailScreenScaffold(
            titleResId = R.string.settings_usage_guide_title,
            onBackClick = {},
            bottomContent = {
                SettingsDocumentButton(
                    text = stringResource(R.string.settings_service_info_privacy_title),
                    onClick = {},
                )
            },
        ) {
            SettingsGuideCard(
                icon = Icons.Outlined.CheckBox,
                titleResId = R.string.settings_usage_guide_select_title,
                descriptionResId = R.string.settings_usage_guide_select_description,
                iconTint = MaterialTheme.colorScheme.primary,
                iconContainerColor = RecapBlue50,
            )
            SettingsGuideCard(
                icon = Icons.Outlined.ErrorOutline,
                titleResId = R.string.settings_usage_guide_permission_title,
                descriptionResId = R.string.settings_usage_guide_permission_description,
                iconTint = SettingsDetailTokens.WarningIconColor,
                iconContainerColor = SettingsDetailTokens.WarningContainerColor,
            )
        }
    }
}

@Preview(name = "Settings Document Button", showBackground = true, widthDp = 360)
@Composable
private fun SettingsDocumentButtonPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsDocumentButton(
            text = stringResource(R.string.settings_service_info_privacy_title),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Settings Guide Card", showBackground = true, widthDp = 360)
@Composable
private fun SettingsGuideCardPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsGuideCard(
            icon = Icons.Outlined.CheckBox,
            titleResId = R.string.settings_usage_guide_select_title,
            descriptionResId = R.string.settings_usage_guide_select_description,
            iconTint = MaterialTheme.colorScheme.primary,
            iconContainerColor = RecapBlue50,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Settings Data Card", showBackground = true, widthDp = 360)
@Composable
private fun SettingsDataCardPreview() {
    RECAPTheme(dynamicColor = false) {
        SettingsDataCard(
            labelResId = R.string.settings_data_management_organized_label,
            titleResId = R.string.settings_data_management_organized_title,
            descriptionResId = R.string.settings_data_management_organized_description,
            modifier = Modifier.padding(16.dp),
        )
    }
}
