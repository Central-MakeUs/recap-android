package com.chalkak.recap.core.design.component.bottombar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapGray100
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.CupertinoMaterials

enum class RecapBottomBarDestination(
    @get:StringRes val labelResId: Int,
    @get:DrawableRes val iconResId: Int,
) {
    Home(
        labelResId = R.string.bottom_nav_home,
        iconResId = R.drawable.ic_home_24,
    ),
    Collection(
        labelResId = R.string.bottom_nav_collection,
        iconResId = R.drawable.ic_storage_24,
    ),
}

@Composable
fun RecapBottomBar(
    hazeState: HazeState,
    currentDestination: RecapBottomBarDestination,
    onDestinationClick: (RecapBottomBarDestination) -> Unit,
    onOrganizeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = RecapBottomBarHorizontalPadding,
                end = RecapBottomBarHorizontalPadding,
                bottom = RecapBottomBarBottomPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RecapBottomBarItemGap),
    ) {
        RecapBottomBarNavPill(
            hazeState = hazeState,
            modifier = Modifier.weight(1f),
            currentDestination = currentDestination,
            onDestinationClick = onDestinationClick,
        )

        RecapOrganizeButton(onClick = onOrganizeClick)
    }
}

@Composable
private fun RecapBottomBarNavPill(
    hazeState: HazeState,
    currentDestination: RecapBottomBarDestination,
    onDestinationClick: (RecapBottomBarDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(percent = 50)
    val pillBlurStyle = CupertinoMaterials.ultraThin()

    Box(
        modifier = modifier
            .shadow(
                elevation = RecapBottomBarDefaults.GlassShadowElevation,
                shape = pillShape,
                ambientColor = RecapBottomBarDefaults.GlassShadowColor,
                spotColor = RecapBottomBarDefaults.GlassShadowColor,
            )
            .hazeEffect(state = hazeState) {
                blurEffect {
                    blurEnabled = true
                    blurRadius = 20.dp
                    style = pillBlurStyle
                    colorEffects = listOf(
                        HazeColorEffect.tint(
                            Color.White.copy(alpha = RecapBottomBarDefaults.GlassTintAlpha),
                        ),
                    )
                    noiseFactor = 0.05f
                }
            }
            .border(
                width = 1.dp,
                color = RecapBottomBarDefaults.GlassBorderColor,
                shape = pillShape,
            )
            .clip(pillShape),
    ) {
        Row(
            modifier = Modifier
                .height(RecapBottomBarHeight)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecapBottomBarItem(
                labelResId = RecapBottomBarDestination.Home.labelResId,
                icon = painterResource(RecapBottomBarDestination.Home.iconResId),
                selected = currentDestination == RecapBottomBarDestination.Home,
                onClick = { onDestinationClick(RecapBottomBarDestination.Home) },
                modifier = Modifier.weight(1f),
            )
            RecapBottomBarItem(
                labelResId = RecapBottomBarDestination.Collection.labelResId,
                icon = painterResource(RecapBottomBarDestination.Collection.iconResId),
                selected = currentDestination == RecapBottomBarDestination.Collection,
                onClick = { onDestinationClick(RecapBottomBarDestination.Collection) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RecapBottomBarItem(
    @StringRes labelResId: Int,
    icon: Painter,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val itemShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .height(RecapBottomBarItemHeight)
            .clip(itemShape)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(
                        alpha = RecapBottomBarDefaults.GlassSelectedTintAlpha,
                    )
                } else {
                    Color.Transparent
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(labelResId),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RecapOrganizeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(RecapOrganizeButtonSize)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.outlineVariant,
            )
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus_32),
            contentDescription = stringResource(R.string.bottom_nav_organize),
            modifier = Modifier.size(28.dp),
            tint = Color.White,
        )
    }
}

object RecapBottomBarDefaults {
    val Height: Dp = 64.dp
    val BottomPadding: Dp = 16.dp
    val ContentScrollPadding: Dp = (Height + BottomPadding) * 2

    val GlassBorderColor: Color = Color.White.copy(alpha = 0.35f)
    val GlassShadowColor: Color = Color.Black.copy(alpha = 0.08f)
    val GlassShadowElevation: Dp = 8.dp
    const val GlassTintAlpha: Float = 0.15f
    const val GlassSelectedTintAlpha: Float = 0.08f
}

private val RecapBottomBarHeight: Dp = RecapBottomBarDefaults.Height
private val RecapBottomBarItemHeight: Dp = 52.dp
private val RecapOrganizeButtonSize: Dp = 56.dp
private val RecapBottomBarItemGap: Dp = 12.dp
private val RecapBottomBarHorizontalPadding: Dp = 16.dp
private val RecapBottomBarBottomPadding: Dp = RecapBottomBarDefaults.BottomPadding

@Preview(name = "Recap Bottom Bar - Home", showBackground = true, widthDp = 360, heightDp = 140)
@Composable
private fun RecapBottomBarHomePreview() {
    RECAPTheme {
        val hazeState = rememberHazeState()
        RecapBottomBarGlassPreviewBackground(hazeState = hazeState) {
            RecapBottomBar(
                hazeState = hazeState,
                currentDestination = RecapBottomBarDestination.Home,
                onDestinationClick = {},
                onOrganizeClick = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Preview(name = "Recap Bottom Bar - Collection", showBackground = true, widthDp = 360, heightDp = 140)
@Composable
private fun RecapBottomBarCollectionPreview() {
    RECAPTheme {
        val hazeState = rememberHazeState()
        RecapBottomBarGlassPreviewBackground(hazeState = hazeState) {
            RecapBottomBar(
                hazeState = hazeState,
                currentDestination = RecapBottomBarDestination.Collection,
                onDestinationClick = {},
                onOrganizeClick = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun RecapBottomBarGlassPreviewBackground(
    hazeState: HazeState,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .hazeSource(state = hazeState)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(RecapBlue50, RecapGray100),
                ),
            ),
        content = content,
    )
}
