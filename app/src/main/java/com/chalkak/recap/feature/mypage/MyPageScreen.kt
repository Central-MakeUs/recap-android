package com.chalkak.recap.feature.mypage

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.BuildConfig
import com.chalkak.recap.R
import com.chalkak.recap.core.design.component.RecapButton
import com.chalkak.recap.core.design.component.RecapTopBar

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    uiState: MyPageUiState = MyPageUiState(),
    onAction: (MyPageAction) -> Unit = {},
) {
    var debugLoginOverride by rememberSaveable(uiState.isLoggedIn) {
        mutableStateOf(uiState.isLoggedIn)
    }
    val isLoggedIn = if (BuildConfig.DEBUG) debugLoginOverride else uiState.isLoggedIn

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(R.string.bottom_nav_my_page),
                onBackClick = { onAction(MyPageAction.NavigateBack) },
                backButtonContentDescription = stringResource(
                    R.string.my_page_back_content_description,
                ),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MyPageTokens.HorizontalPadding)
                    .padding(top = MyPageTokens.ContentTopPadding),
                verticalArrangement = Arrangement.spacedBy(MyPageTokens.SectionSpacing),
            ) {
                if (isLoggedIn) {
                    SignedInMenu(
                        onAction = onAction,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    SignedOutMenu(
                        onAction = onAction,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (BuildConfig.DEBUG) {
                    DebugLoginOverrideRow(
                        checked = debugLoginOverride,
                        onCheckedChange = { debugLoginOverride = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SignedInMenu(
    onAction: (MyPageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        MyPageMenuItemData(
            icon = Icons.Outlined.Notifications,
            titleResId = R.string.my_page_notification_settings_title,
            descriptionResId = R.string.my_page_notification_settings_description,
            onClick = { onAction(MyPageAction.OpenNotificationSettings) },
        ),
        MyPageMenuItemData(
            icon = Icons.Outlined.CloudUpload,
            titleResId = R.string.my_page_upload_guide_title,
            descriptionResId = R.string.my_page_upload_guide_signed_in_description,
            onClick = { onAction(MyPageAction.OpenUploadGuide) },
        ),
        MyPageMenuItemData(
            icon = Icons.Outlined.Storage,
            titleResId = R.string.my_page_data_management_title,
            descriptionResId = R.string.my_page_data_management_description,
            onClick = { onAction(MyPageAction.OpenDataManagement) },
        ),
        MyPageMenuItemData(
            icon = Icons.Outlined.PrivacyTip,
            titleResId = R.string.my_page_privacy_guide_title,
            descriptionResId = R.string.my_page_privacy_guide_description,
            onClick = { onAction(MyPageAction.OpenPrivacyGuide) },
        ),
        MyPageMenuItemData(
            icon = Icons.Outlined.Info,
            titleResId = R.string.my_page_service_info_title,
            descriptionResId = R.string.my_page_service_info_description,
            onClick = { onAction(MyPageAction.OpenServiceInfo) },
        ),
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MyPageTokens.SectionSpacing),
    ) {
        MyPageMenuSection {
            MyPageMenuSurface(position = MyPageMenuItemPosition.Single) {
                SignedInProfileContent()
            }
        }
        MyPageMenuItemsSection(items = items)
    }
}

@Composable
private fun SignedOutMenu(
    onAction: (MyPageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        MyPageMenuItemData(
            icon = Icons.Outlined.Notifications,
            titleResId = R.string.my_page_notification_settings_title,
            descriptionResId = R.string.my_page_notification_settings_description,
            onClick = { onAction(MyPageAction.OpenNotificationSettings) },
        ),
        MyPageMenuItemData(
            icon = Icons.Outlined.CloudUpload,
            titleResId = R.string.my_page_upload_guide_signed_out_title,
            descriptionResId = R.string.my_page_upload_guide_signed_out_description,
            onClick = { onAction(MyPageAction.OpenUploadGuide) },
        ),
        MyPageMenuItemData(
            icon = Icons.Outlined.Info,
            titleResId = R.string.my_page_service_info_title,
            descriptionResId = R.string.my_page_service_info_description,
            onClick = { onAction(MyPageAction.OpenServiceInfo) },
        ),
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MyPageTokens.SectionSpacing),
    ) {
        MyPageMenuSection {
            MyPageMenuSurface(position = MyPageMenuItemPosition.Single) {
                SignedOutLoginContent(
                    onLoginClick = { onAction(MyPageAction.Login) },
                )
            }
        }
        MyPageMenuItemsSection(items = items)
    }
}

@Composable
private fun SignedInProfileContent(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(MyPageTokens.ProfileCardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProfileIcon(
            containerSize = 54.dp,
            iconSize = 30.dp,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            iconColor = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.my_page_signed_in_user_name),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.my_page_signed_in_status),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SignedOutLoginContent(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20 .dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileIcon(
            containerSize = 64.dp,
            iconSize = 32.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.my_page_login_required_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.my_page_login_required_description),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        RecapButton(
            text = stringResource(R.string.my_page_login_button),
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth(),
            shadowElevation = 8.dp,
        )
    }
}

@Composable
private fun MyPageMenuSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(MyPageTokens.CardRadius))
            .background(MaterialTheme.colorScheme.surface),
        content = content,
    )
}

@Composable
private fun MyPageMenuItemsSection(
    items: List<MyPageMenuItemData>,
    modifier: Modifier = Modifier,
) {
    MyPageMenuSection(modifier = modifier) {
        items.forEachIndexed { index, item ->
            MyPageMenuItem(
                item = item,
                position = when {
                    items.size == 1 -> MyPageMenuItemPosition.Single
                    index == 0 -> MyPageMenuItemPosition.Top
                    index == items.lastIndex -> MyPageMenuItemPosition.Bottom
                    else -> MyPageMenuItemPosition.Middle
                },
            )
            if (index != items.lastIndex) {
                MyPageMenuDivider()
            }
        }
    }
}

@Composable
private fun MyPageMenuSurface(
    position: MyPageMenuItemPosition,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = position.toShape()
    if (onClick == null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            content = content,
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            content = content,
        )
    }
}

@Composable
private fun MyPageMenuItem(
    item: MyPageMenuItemData,
    position: MyPageMenuItemPosition,
    modifier: Modifier = Modifier,
) {
    MyPageMenuSurface(
        position = position,
        onClick = item.onClick,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = stringResource(item.titleResId),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(item.descriptionResId),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun MyPageMenuDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier.padding(0.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun ProfileIcon(
    containerSize: Dp,
    iconSize: Dp,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = iconColor,
        )
    }
}

@Composable
private fun DebugLoginOverrideRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(MyPageTokens.CardRadius),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.my_page_debug_login_override_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.my_page_debug_login_override_description),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

private data class MyPageMenuItemData(
    val icon: ImageVector,
    @get:StringRes val titleResId: Int,
    @get:StringRes val descriptionResId: Int,
    val onClick: () -> Unit,
)

private enum class MyPageMenuItemPosition {
    Single,
    Top,
    Middle,
    Bottom,
}

private fun MyPageMenuItemPosition.toShape(): Shape {
    return when (this) {
        MyPageMenuItemPosition.Single -> RoundedCornerShape(MyPageTokens.CardRadius)
        MyPageMenuItemPosition.Top -> RoundedCornerShape(
            topStart = MyPageTokens.CardRadius,
            topEnd = MyPageTokens.CardRadius,
        )
        MyPageMenuItemPosition.Middle -> RoundedCornerShape(0.dp)
        MyPageMenuItemPosition.Bottom -> RoundedCornerShape(
            bottomStart = MyPageTokens.CardRadius,
            bottomEnd = MyPageTokens.CardRadius,
        )
    }
}

private object MyPageTokens {
    val HorizontalPadding = 16.dp
    val ContentTopPadding = 12.dp
    val SectionSpacing = 16.dp
    val CardRadius = 14.dp
    val ProfileCardPadding = 16.dp
}
