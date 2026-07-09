package com.chalkak.recap.core.design.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.R
import com.chalkak.recap.core.design.category.RecapCategoryType
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue50
import com.chalkak.recap.core.design.theme.RecapGray900
import com.chalkak.recap.core.design.theme.White
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.CupertinoMaterials
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * 뒤쪽에 radius가 적용된 둥근 직사각형 border 레이어를 두고,
 * 그 위에 haze glass 효과를 입힌 폴더 모양 레이어를 겹쳐 표현하는 카드.
 *
 * [category]의 border/content/tint 색과 아이콘을 사용한다.
 * tint는 Figma/SVG와 같이 좌하단이 강하고 우상단 바깥으로 빠지는 대각 linear gradient다.
 */
@Composable
fun RecapHazeFolderCard(
    category: RecapCategoryType,
    recapCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    val hazeState = rememberHazeState()
    val glassStyle = CupertinoMaterials.ultraThin()
    val interactionSource = remember { MutableInteractionSource() }
    val backShape = RoundedCornerShape(RecapHazeFolderCardTokens.BackCornerRadius)
    val folderShape = remember { FolderShape }
    val tintBrush = remember(category.tintColor) {
        folderTintBrush(category.tintColor)
    }
    val recapLabel = pluralStringResource(
        R.plurals.recap_haze_folder_card_recap_label,
        recapCount,
    )
    val safeScale = scale.coerceAtLeast(0.1f)

    Box(
        modifier = modifier
            .size(
                width = RecapHazeFolderCardTokens.CardWidth * safeScale,
                height = RecapHazeFolderCardTokens.CardHeight * safeScale,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(
                    width = RecapHazeFolderCardTokens.CardWidth,
                    height = RecapHazeFolderCardTokens.CardHeight,
                )
                .graphicsLayer {
                    scaleX = safeScale
                    scaleY = safeScale
                    transformOrigin = TransformOrigin(0f, 0f)
                },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(
                        width = RecapHazeFolderCardTokens.BackWidth,
                        height = RecapHazeFolderCardTokens.BackHeight,
                    )
                    .clip(backShape)
                    .hazeSource(hazeState)
                    .border(
                        width = RecapHazeFolderCardTokens.BackBorderWidth,
                        color = category.borderColor,
                        shape = backShape,
                    ),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = RecapHazeFolderCardTokens.FolderLayerOffsetX,
                        y = RecapHazeFolderCardTokens.FolderLayerOffsetY,
                    )
                    .size(
                        width = RecapHazeFolderCardTokens.FolderWidth,
                        height = RecapHazeFolderCardTokens.FolderHeight,
                    )
                    .clip(folderShape)
                    .hazeEffect(state = hazeState) {
                        blurEffect {
                            blurEnabled = true
                            blurRadius = RecapHazeFolderCardTokens.BlurRadius
                            style = glassStyle
                            colorEffects = listOf(
                                HazeColorEffect.tint(brush = tintBrush),
                            )
                            noiseFactor = RecapHazeFolderCardTokens.NoiseFactor
                        }
                    }
                    .border(
                        width = RecapHazeFolderCardTokens.FrontBorderWidth,
                        color = RecapHazeFolderCardTokens.GlassBorderColor,
                        shape = folderShape,
                    ),
            ) {
                Icon(
                    painter = painterResource(category.iconResId),
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(RecapHazeFolderCardTokens.ContentPadding)
                        .size(RecapHazeFolderCardTokens.IconSize),
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(RecapHazeFolderCardTokens.ContentPadding),
                ) {
                    Text(
                        text = recapCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = category.contentColor,
                    )
                    Text(
                        text = " $recapLabel",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = category.contentColor,
                    )
                }
            }
        }
    }
}

/**
 * Figma Vector.svg path를 파싱한 폴더 모양 [Shape].
 * viewBox(94 x 79) 좌표를 대상 [Size]에 맞춰 스케일한다.
 */
private object FolderShape : Shape {
    private val sourcePath: Path = PathParser()
        .parsePathString(RecapHazeFolderCardTokens.FolderSvgPath)
        .toPath()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val scaleX = size.width / RecapHazeFolderCardTokens.FolderViewBoxWidth
        val scaleY = size.height / RecapHazeFolderCardTokens.FolderViewBoxHeight
        val scaledPath = Path().apply {
            addPath(sourcePath)
            transform(
                Matrix().apply {
                    scale(x = scaleX, y = scaleY)
                },
            )
        }
        return Outline.Generic(scaledPath)
    }
}

/**
 * SVG fill gradient(paint0)와 같은 축:
 * 좌하단(강함) → 우상단 바깥(약함).
 */
private fun folderTintBrush(tintColor: Color): Brush {
    val colors = listOf(
        tintColor.copy(alpha = RecapHazeFolderCardTokens.GlassTintStartAlpha),
        tintColor.copy(alpha = RecapHazeFolderCardTokens.GlassTintEndAlpha),
    )
    return object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            return LinearGradientShader(
                from = Offset(
                    x = size.width * RecapHazeFolderCardTokens.TintStartXFraction,
                    y = size.height * RecapHazeFolderCardTokens.TintStartYFraction,
                ),
                to = Offset(
                    x = size.width * RecapHazeFolderCardTokens.TintEndXFraction,
                    y = size.height * RecapHazeFolderCardTokens.TintEndYFraction,
                ),
                colors = colors,
            )
        }
    }
}

private object RecapHazeFolderCardTokens {
    val CardWidth = 99.dp
    val CardHeight = 88.dp
    val BackWidth = 93.dp
    val BackHeight = 71.dp
    val BackCornerRadius = 7.41.dp
    val FolderWidth = 94.dp
    val FolderHeight = 79.dp
    val FolderLayerOffsetX = 5.dp
    val FolderLayerOffsetY = 9.dp
    const val FolderViewBoxWidth = 94f
    const val FolderViewBoxHeight = 79f
    val BackBorderWidth = 4.dp
    val FrontBorderWidth = 0.34.dp
    val BlurRadius = 10.dp
    val ContentPadding = 9.dp
    val IconSize = 16.dp
    const val NoiseFactor = 0.2f

    val GlassBorderColor = White.copy(alpha = 0.5f)
    const val GlassTintStartAlpha = 0.60f
    const val GlassTintEndAlpha = 0.02f

    // SVG paint0: (3.47, 88.11) → (166.95, -22.46) / viewBox 94x79
    const val TintStartXFraction = 3.47289f / FolderViewBoxWidth
    const val TintStartYFraction = 88.108f / FolderViewBoxHeight
    const val TintEndXFraction = 166.945f / FolderViewBoxWidth
    const val TintEndYFraction = -22.4553f / FolderViewBoxHeight

    const val FolderSvgPath =
        "M9.40039 0.170898H33.7227C34.9527 0.170919 36.1246 0.413203 37.2402 0.897461C38.3564 1.38198 39.3359 2.06749 40.1787 2.95605V2.95703L46.877 9.99219L46.9268 10.0459H84.5996C87.136 10.0459 89.3058 10.9928 91.1172 12.8955C92.9289 14.7987 93.8322 17.0808 93.8291 19.75V69.125C93.8291 71.7977 92.9258 74.0812 91.1172 75.9844C89.3091 77.8869 87.14 78.8322 84.6006 78.8291H9.40039C6.86373 78.8291 4.69496 77.884 2.88672 75.9844C1.0783 74.0846 0.174028 71.801 0.170898 69.125V9.875C0.170898 7.20236 1.07513 4.9204 2.88672 3.02051C4.69821 1.12075 6.86687 0.174128 9.40039 0.170898Z"
}


@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Recap Haze Folder Cards All", showBackground = true, widthDp = 300, heightDp = 600)
@Composable
private fun RecapHazeFolderCardsAllPreview() {
    RECAPTheme(dynamicColor = false) {
        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(RecapBlue50, White),
                    ),
                )
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            maxItemsInEachRow = 3,
        ) {
            RecapHazeFolderCardPreviewItems.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.width(RecapHazeFolderCardTokens.CardWidth),
                ) {
                    RecapHazeFolderCard(
                        category = item.category,
                        recapCount = item.recapCount,
                        onClick = {},
                    )
                    Text(
                        text = stringResource(item.category.labelResId),
                        style = MaterialTheme.typography.titleSmall,
                        color = RecapGray900,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private data class RecapHazeFolderCardPreviewItem(
    val category: RecapCategoryType,
    val recapCount: Int,
)

private val RecapHazeFolderCardPreviewItems = listOf(
    RecapHazeFolderCardPreviewItem(RecapCategoryType.ShoppingProduct, 20),
    RecapHazeFolderCardPreviewItem(RecapCategoryType.PlaceRestaurant, 23),
    RecapHazeFolderCardPreviewItem(RecapCategoryType.ScheduleReservation, 10),
    RecapHazeFolderCardPreviewItem(RecapCategoryType.InfoKnowledge, 12),
    RecapHazeFolderCardPreviewItem(RecapCategoryType.BookContent, 1),
    RecapHazeFolderCardPreviewItem(RecapCategoryType.BenefitEvent, 5),
    RecapHazeFolderCardPreviewItem(RecapCategoryType.RecordCapture, 12),
)
