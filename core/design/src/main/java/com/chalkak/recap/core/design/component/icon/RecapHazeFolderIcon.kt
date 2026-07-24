package com.chalkak.recap.core.design.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.chalkak.recap.core.design.theme.RECAPTheme
import com.chalkak.recap.core.design.theme.RecapBlue300
import com.chalkak.recap.core.design.theme.White
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.blur.materials.CupertinoMaterials
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToInt

/**
 * Group 63.svg 기반 헤이즈 폴더 아이콘.
 *
 * [RecapHazeFolderCard]와 같이 뒤 레이어를 [hazeSource], 앞 폴더 글래스를
 * [CupertinoMaterials.ultraThin] [hazeEffect]로 구성한다.
 *
 * [size]는 렌더링뿐 아니라 레이아웃에서 차지하는 크기도 함께 반영한다 (viewBox 190 기준 스케일).
 */
@Composable
fun RecapHazeFolderIcon(
    modifier: Modifier = Modifier,
    size: Dp = RecapHazeFolderIconTokens.DefaultSize,
    contentDescription: String? = null,
) {
    val hazeState = rememberHazeState()
    val glassStyle = CupertinoMaterials.ultraThin()
    val folderShape = remember { FolderShape }
    val tintBrush = remember { folderTintBrush() }
    val backShape = RoundedCornerShape(RecapHazeFolderIconTokens.BackCornerRadius)
    val scale = (size / RecapHazeFolderIconTokens.DefaultSize).coerceAtLeast(0.1f)
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(semanticsModifier)
            .recapHazeFolderIconScale(scale),
    ) {
        Box(
            modifier = Modifier.size(RecapHazeFolderIconTokens.DefaultSize),
        ) {
            // Back: hazeSource가 background를 감싸야 fill이 캡처된다
            // (HomeScreen/RecapBottomBar/Card와 동일 — hazeSource 다음이 그려질 콘텐츠)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = RecapHazeFolderIconTokens.BackOffsetX,
                        y = RecapHazeFolderIconTokens.BackOffsetY,
                    )
                    .size(
                        width = RecapHazeFolderIconTokens.BackWidth,
                        height = RecapHazeFolderIconTokens.BackHeight,
                    )
                    .clip(backShape)
                    .hazeSource(hazeState)
                    .background(RecapHazeFolderIconTokens.BackFillColor),
            )

            // Front folder glass: ultraThin hazeEffect (card와 동일 구조)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = RecapHazeFolderIconTokens.FolderOffsetX,
                        y = RecapHazeFolderIconTokens.FolderOffsetY,
                    )
                    .size(
                        width = RecapHazeFolderIconTokens.FolderWidth,
                        height = RecapHazeFolderIconTokens.FolderHeight,
                    )
                    .clip(folderShape)
                    .hazeEffect(state = hazeState) {
                        inputScale = HazeInputScale.Fixed(0.5f)
                        blurEffect {
                            blurEnabled = true
                            blurRadius = RecapHazeFolderIconTokens.BlurRadius
                            style = glassStyle
                            colorEffects = listOf(
                                HazeColorEffect.tint(brush = tintBrush),
                            )
                            noiseFactor = RecapHazeFolderIconTokens.NoiseFactor
                        }
                    }
                    .border(
                        width = RecapHazeFolderIconTokens.FrontBorderWidth,
                        color = RecapHazeFolderIconTokens.GlassBorderColor,
                        shape = folderShape,
                    ),
            )

            CornerBracket(
                pathData = RecapHazeFolderIconTokens.TopLeftCornerPath,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(
                        width = RecapHazeFolderIconTokens.CornerWidth,
                        height = RecapHazeFolderIconTokens.CornerHeight,
                    ),
            )
            CornerBracket(
                pathData = RecapHazeFolderIconTokens.TopRightCornerPath,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(
                        width = RecapHazeFolderIconTokens.CornerWidth,
                        height = RecapHazeFolderIconTokens.CornerHeight,
                    ),
            )
            CornerBracket(
                pathData = RecapHazeFolderIconTokens.BottomLeftCornerPath,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(
                        width = RecapHazeFolderIconTokens.CornerWidth,
                        height = RecapHazeFolderIconTokens.CornerHeight,
                    ),
            )
            CornerBracket(
                pathData = RecapHazeFolderIconTokens.BottomRightCornerPath,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(
                        width = RecapHazeFolderIconTokens.CornerWidth,
                        height = RecapHazeFolderIconTokens.CornerHeight,
                    ),
            )

            // 눈은 glass 위 sibling Box (blur 대상 아님)
            Eye(
                scleraSize = RecapHazeFolderIconTokens.LeftScleraSize,
                pupilSize = RecapHazeFolderIconTokens.PupilSize,
                scleraOffsetX = RecapHazeFolderIconTokens.LeftScleraOffsetX,
                scleraOffsetY = RecapHazeFolderIconTokens.LeftScleraOffsetY,
                pupilOffsetX = RecapHazeFolderIconTokens.LeftPupilOffsetX,
                pupilOffsetY = RecapHazeFolderIconTokens.LeftPupilOffsetY,
            )
            Eye(
                scleraSize = RecapHazeFolderIconTokens.RightScleraSize,
                pupilSize = RecapHazeFolderIconTokens.PupilSize,
                scleraOffsetX = RecapHazeFolderIconTokens.RightScleraOffsetX,
                scleraOffsetY = RecapHazeFolderIconTokens.RightScleraOffsetY,
                pupilOffsetX = RecapHazeFolderIconTokens.RightPupilOffsetX,
                pupilOffsetY = RecapHazeFolderIconTokens.RightPupilOffsetY,
            )
        }
    }
}

@Composable
private fun CornerBracket(
    pathData: String,
    modifier: Modifier = Modifier,
) {
    val path = remember(pathData) {
        PathParser().parsePathString(pathData).toPath()
    }
    Canvas(modifier = modifier) {
        // SVG 1 unit = 1.dp. PathParser 좌표를 density에 맞게 스케일한다.
        val unitPx = 1.dp.toPx()
        withTransform({
            scale(scaleX = unitPx, scaleY = unitPx, pivot = Offset.Zero)
        }) {
            drawPath(
                path = path,
                color = RecapHazeFolderIconTokens.FrameColor,
                style = Stroke(
                    width = RecapHazeFolderIconTokens.CornerStrokeWidthValue,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}

@Composable
private fun Eye(
    scleraSize: Dp,
    pupilSize: Dp,
    scleraOffsetX: Dp,
    scleraOffsetY: Dp,
    pupilOffsetX: Dp,
    pupilOffsetY: Dp,
) {
    Box(
        modifier = Modifier
            .offset(x = scleraOffsetX, y = scleraOffsetY)
            .size(scleraSize)
            .clip(CircleShape)
            .background(RecapHazeFolderIconTokens.EyeScleraColor),
    )
    Box(
        modifier = Modifier
            .offset(x = pupilOffsetX, y = pupilOffsetY)
            .size(pupilSize)
            .clip(CircleShape)
            .background(RecapHazeFolderIconTokens.EyePupilColor),
    )
}

/**
 * 콘텐츠는 원본(190) 크기로 측정한 뒤, 부모 레이아웃에는 [scale]이 반영된 크기를 보고한다.
 */
private fun Modifier.recapHazeFolderIconScale(scale: Float): Modifier = layout { measurable, _ ->
    val widthPx = RecapHazeFolderIconTokens.DefaultSize.roundToPx()
    val heightPx = RecapHazeFolderIconTokens.DefaultSize.roundToPx()
    val scaledWidthPx = (widthPx * scale).roundToInt()
    val scaledHeightPx = (heightPx * scale).roundToInt()

    val placeable = measurable.measure(Constraints.fixed(widthPx, heightPx))
    layout(scaledWidthPx, scaledHeightPx) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}

/**
 * 폴더 path를 FolderOffset 기준 로컬 좌표로 파싱한 [Shape].
 * viewBox(FolderWidth x FolderHeight)에 맞춰 스케일한다.
 */
private object FolderShape : Shape {
    private val sourcePath: Path = PathParser()
        .parsePathString(RecapHazeFolderIconTokens.FolderSvgPathLocal)
        .toPath()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val scaleX = size.width / RecapHazeFolderIconTokens.FolderViewBoxWidth
        val scaleY = size.height / RecapHazeFolderIconTokens.FolderViewBoxHeight
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
 * SVG paint0를 폴더 로컬 좌표로 변환한 tint.
 * 우상단 White(α0.1) → 좌하단 RecapBlue300(α0.7).
 */
private fun folderTintBrush(): Brush {
    val colors = listOf(
        White.copy(alpha = RecapHazeFolderIconTokens.GlassTintStartAlpha),
        RecapBlue300.copy(alpha = RecapHazeFolderIconTokens.GlassTintEndAlpha),
    )
    return object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            return LinearGradientShader(
                from = Offset(
                    x = size.width * RecapHazeFolderIconTokens.TintStartXFraction,
                    y = size.height * RecapHazeFolderIconTokens.TintStartYFraction,
                ),
                to = Offset(
                    x = size.width * RecapHazeFolderIconTokens.TintEndXFraction,
                    y = size.height * RecapHazeFolderIconTokens.TintEndYFraction,
                ),
                colors = colors,
            )
        }
    }
}

private object RecapHazeFolderIconTokens {
    val DefaultSize = 190.dp

    // Back rect (SVG absolute)
    val BackOffsetX = 28.2596.dp
    val BackOffsetY = 38.2896.dp
    val BackWidth = 126.916.dp
    val BackHeight = 96.3232.dp
    val BackCornerRadius = 9.09231.dp
    val BackFillColor = Color(0xFF596AFF)

    // Folder layer bbox from SVG path (minX/minY → maxX/maxY)
    // path: x 35.0741..161.69, y 51.1616..156.877
    val FolderOffsetX = 35.0741.dp
    val FolderOffsetY = 51.1616.dp
    val FolderWidth = 126.6159.dp
    val FolderHeight = 105.7154.dp
    const val FolderViewBoxWidth = 126.6159f
    const val FolderViewBoxHeight = 105.7154f
    const val FolderLocalOriginX = 35.0741f
    const val FolderLocalOriginY = 51.1616f

    val FrameColor = Color(0xFFAAB3FF)
    val GlassBorderColor = White.copy(alpha = 0.5f)
    // Stroke width in SVG units (scaled with path via unitPx)
    const val CornerStrokeWidthValue = 6.53434f

    // Corner frame cells: from viewBox edge (0) to inner arm end (~47 / ~56)
    // Half-stroke (~3.27) sits on the outer edge so brackets hug the 190 square.
    val CornerWidth = 50.dp
    val CornerHeight = 59.dp

    val FrontBorderWidth = 0.3.dp
    val BlurRadius = 12.dp
    const val NoiseFactor = 0.0f

    const val GlassTintStartAlpha = 0.1f
    const val GlassTintEndAlpha = 0.7f

    // paint0 absolute → local fractions
    const val TintStartXFraction = (148.936f - FolderLocalOriginX) / FolderViewBoxWidth
    const val TintStartYFraction = (61.1088f - FolderLocalOriginY) / FolderViewBoxHeight
    const val TintEndXFraction = (36.8887f - FolderLocalOriginX) / FolderViewBoxWidth
    const val TintEndYFraction = (162.51f - FolderLocalOriginY) / FolderViewBoxHeight

    val EyeScleraColor = Color(0xFFF3F3F3)
    val EyePupilColor = Color(0xFF0A0A0A)

    // Eyes: SVG center/radius → Box top-left offset (center - radius)
    val LeftScleraSize = 42.3026.dp
    val LeftScleraOffsetX = (74.3537f - 21.1513f).dp
    val LeftScleraOffsetY = (100.171f - 21.1513f).dp
    val LeftPupilOffsetX = (79.9746f - 10.8083f).dp
    val LeftPupilOffsetY = (99.9112f - 10.8083f).dp

    val RightScleraSize = 42.0032.dp
    val RightScleraOffsetX = (111.675f - 21.0016f).dp
    val RightScleraOffsetY = (95.6069f - 21.0016f).dp
    val RightPupilOffsetX = (118.815f - 10.8083f).dp
    val RightPupilOffsetY = (94.5707f - 10.8083f).dp
    val PupilSize = 21.6166.dp

    // Absolute SVG path translated by (-FolderLocalOriginX, -FolderLocalOriginY)
    const val FolderSvgPathLocal =
        "M12.541 0H45.3808C47.0528 0 48.6464 0.3271 50.1631 0.9815C51.68 1.636 53.011 2.5626 54.1562 3.7627V3.7637L63.1992 13.2051L63.2441 13.252H114.0739C117.5219 13.252 120.4719 14.5327 122.9329 17.1016C125.3929 19.6706 126.6199 22.7519 126.6159 26.3535V92.6134C126.6159 96.2194 125.3889 99.3044 122.9329 101.8734C120.4769 104.4414 117.5259 105.7204 114.0739 105.7154H12.541C9.0931 105.7154 6.1453 104.4364 3.6894 101.8724C1.2334 99.3074 0.0042 96.2244 0 92.6134V13.1016C0 9.4954 1.2291 6.4142 3.6894 3.8496C6.1496 1.2852 9.0974 0.0045 12.541 0Z"

    // Corner paths in local coordinates of each CornerWidth x CornerHeight cell
    // Absolute SVG → local: top-left subtract (0,0); top-right subtract (140,0);
    // bottom-left subtract (0,131); bottom-right subtract (140,131)
    // Corner cell origin: TL(0,0), TR(140,0), BL(0,131), BR(140,131) within 190 viewBox
    // (190-50=140, 190-59=131)
    const val TopLeftCornerPath =
        "M47.0577 3.26709L28.1195 3.26709C14.3939 3.26709 3.26718 14.3938 3.26718 28.1194L3.26718 56.2079"
    const val TopRightCornerPath =
        "M2.477 3.26709H21.415C35.14 3.26709 46.267 14.3938 46.267 28.1194V56.2079"
    const val BottomLeftCornerPath =
        "M47.0577 55.266L28.1195 55.266C14.3939 55.266 3.26718 44.139 3.26718 30.413L3.26719 2.325"
    const val BottomRightCornerPath =
        "M2.477 55.266H21.415C35.14 55.266 46.267 44.139 46.267 30.413V2.325"
}

@Preview(name = "Recap Haze Folder Icon", showBackground = true, widthDp = 220, heightDp = 220)
@Composable
private fun RecapHazeFolderIconPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapHazeFolderIcon(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Recap Haze Folder Icon Scaled", showBackground = true, widthDp = 140, heightDp = 140)
@Composable
private fun RecapHazeFolderIconScaledPreview() {
    RECAPTheme(dynamicColor = false) {
        RecapHazeFolderIcon(
            modifier = Modifier.padding(16.dp),
            size = 96.dp,
        )
    }
}
