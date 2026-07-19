package com.chalkak.recap.feature.settings.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.component.button.RecapButton
import com.chalkak.recap.core.design.component.button.RecapButtonDefaults
import com.chalkak.recap.core.design.component.button.RecapButtonSize
import com.chalkak.recap.core.design.component.divider.RecapSectionDivider
import com.chalkak.recap.core.design.component.popup.RecapPopup
import com.chalkak.recap.core.design.component.topbar.RecapTopBar
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBackground
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.RecapError
import com.chalkak.recap.core.design.theme.RecapGray100
import com.chalkak.recap.core.design.theme.RecapGray50
import com.chalkak.recap.core.design.theme.RecapGray500
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody1
import com.chalkak.recap.core.design.theme.RecapTypography.RecapBody2
import com.chalkak.recap.feature.settings.AccountManagementAction
import com.chalkak.recap.feature.settings.AccountManagementDialog
import com.chalkak.recap.feature.settings.AccountManagementUiState

private val KakaoYellow = Color(0xFFFEE500)

@Composable
fun AccountManagementScreen(
    uiState: AccountManagementUiState,
    onAction: (AccountManagementAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = RecapBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RecapTopBar(
                title = stringResource(R.string.settings_account_management_title),
                onBackClick = { onAction(AccountManagementAction.NavigateBack) },
                backButtonContentDescription = stringResource(
                    R.string.settings_back_content_description,
                ),
                containerColor = RecapBackground,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AccountManagementTokens.HorizontalPadding),
                ) {
                    Spacer(modifier = Modifier.height(AccountManagementTokens.SectionHeaderTopPadding))
                    Text(
                        text = stringResource(R.string.settings_account_login_info_section),
                        style = RecapBody2,
                        color = RecapGray500,
                    )
                    Spacer(modifier = Modifier.height(AccountManagementTokens.SectionHeaderBottomPadding))
                    AccountLoginInfoRow(
                        joinedDate = uiState.joinedDate,
                    )
                    Spacer(modifier = Modifier.height(AccountManagementTokens.DividerTopPadding))
                }
                RecapSectionDivider()
                Spacer(
                    modifier = Modifier.height(
                        AccountManagementTokens.DividerBottomPadding -
                                AccountManagementTokens.ClickAreaVerticalPadding,
                    ),
                )
                AccountLogoutRow(
                    onClick = { onAction(AccountManagementAction.LogoutClick) },
                )
            }
            RecapButton(
                text = stringResource(R.string.settings_account_withdraw),
                onClick = { onAction(AccountManagementAction.WithdrawClick) },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = AccountManagementTokens.WithdrawBottomPadding),
                size = RecapButtonSize.Medium,
                colors = RecapButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    contentColor = RecapError,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = RecapError.copy(alpha = 0.38f),
                ),
                textStyle = RecapBody1,
            )
        }
    }

    when (uiState.dialog) {
        AccountManagementDialog.None -> Unit
        AccountManagementDialog.Logout -> {
            RecapPopup(
                title = stringResource(R.string.logout_confirmation_title),
                description = stringResource(R.string.logout_confirmation_description),
                confirmButtonText = stringResource(R.string.logout_confirmation_logout_button),
                cancelButtonText = stringResource(R.string.logout_confirmation_cancel_button),
                onConfirmClick = { onAction(AccountManagementAction.ConfirmLogout) },
                onCancelClick = { onAction(AccountManagementAction.DismissDialog) },
                onDismissRequest = { onAction(AccountManagementAction.DismissDialog) },
                confirmButtonColor = RecapBlue300,
            )
        }

        AccountManagementDialog.Withdraw -> {
            RecapPopup(
                title = stringResource(R.string.withdrawal_confirmation_title),
                description = stringResource(R.string.withdrawal_confirmation_description),
                confirmButtonText = stringResource(R.string.withdrawal_confirmation_withdraw_button),
                cancelButtonText = stringResource(R.string.withdrawal_confirmation_cancel_button),
                onConfirmClick = { onAction(AccountManagementAction.ConfirmWithdraw) },
                onCancelClick = { onAction(AccountManagementAction.DismissDialog) },
                onDismissRequest = { onAction(AccountManagementAction.DismissDialog) },
                confirmButtonColor = RecapError,
            )
        }
    }
}

@Composable
private fun AccountLoginInfoRow(
    joinedDate: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AccountManagementTokens.AccountInfoSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AccountManagementTokens.KakaoIconSize)
                .clip(CircleShape)
                .background(KakaoYellow),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.kakao_96px),
                contentDescription = stringResource(
                    R.string.settings_account_kakao_content_description,
                ),
                modifier = Modifier.size(AccountManagementTokens.KakaoGlyphSize),
                tint = Color.Black,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(AccountManagementTokens.AccountTextSpacing),
        ) {
            Text(
                text = stringResource(R.string.settings_account_kakao_login_status),
                style = RecapBody1,
                color = RecapGray900,
            )
            Text(
                text = stringResource(R.string.settings_account_joined_format, joinedDate),
                style = RecapBody2,
                color = RecapGray500,
            )
        }
    }
}

@Composable
private fun AccountLogoutRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressAnimationSpec = tween<Float>(
        durationMillis = AccountManagementTokens.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val colorAnimationSpec = tween<Color>(
        durationMillis = AccountManagementTokens.PressAnimationDurationMillis,
        easing = FastOutSlowInEasing,
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) AccountManagementTokens.PressedScale else 1f,
        animationSpec = pressAnimationSpec,
        label = "account_logout_row_press_scale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (isPressed) RecapGray50 else RecapBackground,
        animationSpec = colorAnimationSpec,
        label = "account_logout_row_container_color",
    )
    val rowShape = RoundedCornerShape(AccountManagementTokens.RowCornerRadius)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AccountManagementTokens.ClickAreaHorizontalPadding)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(rowShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                horizontal = AccountManagementTokens.ContentInsetHorizontal,
                vertical = AccountManagementTokens.ClickAreaVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.settings_account_logout),
            modifier = Modifier.weight(1f),
            style = RecapBody1,
            color = RecapGray900,
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right_24),
            contentDescription = null,
            modifier = Modifier.size(AccountManagementTokens.ChevronSize),
            tint = RecapGray100,
        )
    }
}

private object AccountManagementTokens {
    const val PressedScale = 0.9875f
    const val PressAnimationDurationMillis = 100
    val HorizontalPadding = 28.dp
    val ClickAreaHorizontalPadding = 16.dp
    val ContentItemSpacing = 23.dp
    val ClickAreaVerticalPadding = ContentItemSpacing / 2
    val ContentInsetHorizontal = HorizontalPadding - ClickAreaHorizontalPadding
    val SectionHeaderTopPadding = 32.dp
    val SectionHeaderBottomPadding = 16.dp
    val DividerTopPadding = 24.dp
    val DividerBottomPadding = 24.dp
    val AccountInfoSpacing = 12.dp
    val AccountTextSpacing = 4.dp
    val KakaoIconSize = 30.dp
    val KakaoGlyphSize = 16.dp
    val RowCornerRadius = 10.dp
    val ChevronSize = 16.dp
    val WithdrawBottomPadding = 36.dp
}

@Preview(name = "Account Management", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AccountManagementScreenPreview() {
    RECAPTheme(dynamicColor = false) {
        AccountManagementScreen(
            uiState = AccountManagementUiState(
                joinedDate = stringResource(R.string.settings_account_preview_joined_date),
            ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Account Management Logout Dialog",
    showBackground = true,
    widthDp = 360,
    heightDp = 780
)
@Composable
private fun AccountManagementLogoutDialogPreview() {
    RECAPTheme(dynamicColor = false) {
        AccountManagementScreen(
            uiState = AccountManagementUiState(
                joinedDate = stringResource(R.string.settings_account_preview_joined_date),
                dialog = AccountManagementDialog.Logout,
            ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Account Management Withdraw Dialog",
    showBackground = true,
    widthDp = 360,
    heightDp = 780
)
@Composable
private fun AccountManagementWithdrawDialogPreview() {
    RECAPTheme(dynamicColor = false) {
        AccountManagementScreen(
            uiState = AccountManagementUiState(
                joinedDate = stringResource(R.string.settings_account_preview_joined_date),
                dialog = AccountManagementDialog.Withdraw,
            ),
            onAction = {},
        )
    }
}
