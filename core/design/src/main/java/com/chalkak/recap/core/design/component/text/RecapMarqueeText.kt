package com.chalkak.recap.core.design.component.text

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MarqueeDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy as GraphicsCompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object RecapMarqueeDefaults {
    val EdgeFadeWidth = 8.dp
}

/**
 * Scrolls overflowing single-line text with a [androidx.compose.foundation.basicMarquee]-compatible
 * animation, and applies edge fade alphas:
 * - Fitted text: no fades
 * - Overflowing but stopped at the start: right fade only
 * - Overflowing and actively scrolling (`offset > 0`): left and right fades
 *
 * [text] / [textStyle] are restart keys when the label changes; layout decides overflow.
 */
fun Modifier.recapMarqueeText(
    text: String,
    textStyle: TextStyle,
    edgeWidth: Dp = RecapMarqueeDefaults.EdgeFadeWidth,
): Modifier = this
    // Required so DstIn edge fades affect only this text, not the parent background.
    .graphicsLayer { compositingStrategy = GraphicsCompositingStrategy.Offscreen }
    .then(
        RecapMarqueeElement(
            text = text,
            textStyle = textStyle,
            edgeWidth = edgeWidth,
        ),
    )

private data class RecapMarqueeElement(
    private val text: String,
    private val textStyle: TextStyle,
    private val edgeWidth: Dp,
) : ModifierNodeElement<RecapMarqueeNode>() {
    override fun create(): RecapMarqueeNode = RecapMarqueeNode(edgeWidth)

    override fun update(node: RecapMarqueeNode) {
        node.update(text = text, textStyle = textStyle, edgeWidth = edgeWidth)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "recapMarqueeText"
        properties["text"] = text
        properties["textStyle"] = textStyle
        properties["edgeWidth"] = edgeWidth
    }
}

private class RecapMarqueeNode(
    edgeWidth: Dp,
) : Modifier.Node(), LayoutModifierNode, DrawModifierNode {
    private var edgeWidth: Dp = edgeWidth
    private var text: String = ""
    private var textStyle: TextStyle = TextStyle.Default

    private var contentWidth by mutableIntStateOf(0)
    private var containerWidth by mutableIntStateOf(0)
    private var animationJob: Job? = null
    private var invalidateJob: Job? = null
    private var contentLayer: GraphicsLayer? = null

    private val offset = Animatable(0f)

    private val spacingPx by derivedStateOf {
        // Matches MarqueeDefaults.Spacing = fractionOfContainer(1/3).
        (containerWidth / 3f).roundToInt()
    }

    fun update(text: String, textStyle: TextStyle, edgeWidth: Dp) {
        this.edgeWidth = edgeWidth
        val labelChanged = this.text != text || this.textStyle != textStyle
        this.text = text
        this.textStyle = textStyle
        if (labelChanged) {
            restartAnimation()
        }
    }

    override fun onAttach() {
        val graphicsContext = requireGraphicsContext()
        contentLayer?.let(graphicsContext::releaseGraphicsLayer)
        contentLayer = graphicsContext.createGraphicsLayer()
        invalidateJob?.cancel()
        invalidateJob = coroutineScope.launch {
            // Ensure every offset frame (and snap-back to 0) invalidates draw even when
            // the value is only consumed for rendering.
            snapshotFlow { offset.value }.collect { invalidateDraw() }
        }
        restartAnimation()
    }

    override fun onDetach() {
        animationJob?.cancel()
        animationJob = null
        invalidateJob?.cancel()
        invalidateJob = null
        val graphicsContext = requireGraphicsContext()
        contentLayer?.let(graphicsContext::releaseGraphicsLayer)
        contentLayer = null
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints.copy(maxWidth = Constraints.Infinity))
        containerWidth = constraints.constrainWidth(placeable.width)
        contentWidth = placeable.width
        return layout(containerWidth, placeable.height) {
            // Matching basicMarquee: keep full content in a layer so draw can pan it.
            placeable.placeWithLayer(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int = 0

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int = measurable.maxIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ): Int = measurable.minIntrinsicHeight(Constraints.Infinity)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int,
    ): Int = measurable.maxIntrinsicHeight(Constraints.Infinity)

    override fun ContentDrawScope.draw() {
        // Read outside GraphicsLayer.record so DrawModifierNode observes Animatable updates.
        val currentOffset = offset.value
        val overflows = contentWidth > containerWidth && containerWidth > 0
        if (!overflows) {
            drawContent()
            return
        }

        val drawHeight = size.height.roundToInt().coerceAtLeast(0)
        val content = contentLayer
        if (content == null) {
            drawContent()
            return
        }

        content.record(size = IntSize(contentWidth, drawHeight)) {
            this@draw.drawContent()
        }

        val secondCopyOffset = (contentWidth + spacingPx).toFloat()
        clipRect(right = containerWidth.toFloat()) {
            translate(left = -currentOffset) {
                drawLayer(content)
                translate(left = secondCopyOffset) {
                    drawLayer(content)
                }
            }
        }

        // Fades are drawn fresh each frame (not baked into a recorded layer), so a snap
        // back to offset 0 cannot leave a stale left-edge fade behind.
        if (currentOffset > LeftFadeOffsetEpsilonPx) {
            drawMarqueeFadedEdge(leftEdge = true, edgeWidth = edgeWidth)
        }
        drawMarqueeFadedEdge(leftEdge = false, edgeWidth = edgeWidth)
    }

    private fun restartAnimation() {
        val oldJob = animationJob
        oldJob?.cancel()
        if (!isAttached) return
        animationJob = coroutineScope.launch {
            oldJob?.join()
            runAnimation()
        }
    }

    private suspend fun runAnimation() {
        if (MarqueeDefaults.Iterations <= 0) return

        withContext(FixedMotionDurationScale) {
            snapshotFlow {
                if (contentWidth <= containerWidth) return@snapshotFlow null
                (contentWidth + spacingPx).toFloat()
            }.collectLatest { contentWithSpacingWidth ->
                if (contentWithSpacingWidth == null) return@collectLatest

                val spec = createMarqueeAnimationSpec(
                    iterations = MarqueeDefaults.Iterations,
                    targetValue = contentWithSpacingWidth,
                    initialDelayMillis = MarqueeDefaults.RepeatDelayMillis,
                    delayMillis = MarqueeDefaults.RepeatDelayMillis,
                    velocity = MarqueeDefaults.Velocity,
                    density = requireDensity(),
                )
                offset.snapTo(0f)
                try {
                    offset.animateTo(contentWithSpacingWidth, spec)
                } finally {
                    offset.snapTo(0f)
                }
            }
        }
    }
}

private const val LeftFadeOffsetEpsilonPx = 0.5f

private fun createMarqueeAnimationSpec(
    iterations: Int,
    targetValue: Float,
    initialDelayMillis: Int,
    delayMillis: Int,
    velocity: Dp,
    density: Density,
): AnimationSpec<Float> {
    val pxPerSec = with(density) { velocity.toPx() }
    val durationMillis = ceil(targetValue / (pxPerSec / 1_000f)).toInt().coerceAtLeast(1)
    val singleSpec = tween<Float>(
        durationMillis = durationMillis,
        easing = LinearEasing,
        delayMillis = delayMillis,
    )
    val startOffset = StartOffset(-delayMillis + initialDelayMillis)
    return if (iterations == Int.MAX_VALUE) {
        infiniteRepeatable(singleSpec, initialStartOffset = startOffset)
    } else {
        repeatable(iterations, singleSpec, initialStartOffset = startOffset)
    }
}

private fun DrawScope.drawMarqueeFadedEdge(leftEdge: Boolean, edgeWidth: Dp) {
    val edgeWidthPx = edgeWidth.toPx()
    drawRect(
        topLeft = Offset(if (leftEdge) 0f else size.width - edgeWidthPx, 0f),
        size = Size(edgeWidthPx, size.height),
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Black),
            startX = if (leftEdge) 0f else size.width,
            endX = if (leftEdge) edgeWidthPx else size.width - edgeWidthPx,
        ),
        blendMode = BlendMode.DstIn,
    )
}

private object FixedMotionDurationScale : MotionDurationScale {
    override val scaleFactor: Float = 1f
}
