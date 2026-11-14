package aktual.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalGraphicsContext
import kotlinx.coroutines.launch

/**
 * Slides between the pages of [host]. Callers swap pages by composing a different one, so the old
 * page is already gone when the new one draws. Each frame is recorded into a layer, and the last
 * one from the old page slides out beside the new page.
 */
internal fun Modifier.slidePages(host: SheetHostState): Modifier = this then SlidePagesElement(host)

private data class SlidePagesElement(private val host: SheetHostState) :
  ModifierNodeElement<SlidePagesNode>() {
  override fun create() = SlidePagesNode(host)

  override fun update(node: SlidePagesNode) {
    node.host = host
  }

  override fun InspectorInfo.inspectableProperties() = Unit
}

private class SlidePagesNode(var host: SheetHostState) :
  Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {
  private var current: GraphicsLayer? = null
  private var outgoing: GraphicsLayer? = null
  private var shownPage: Page? = null
  private var isBack = false
  private var progress = Animatable(1f)

  override fun onDetach() {
    current?.let(::release)
    outgoing?.let(::release)
    current = null
    outgoing = null
    shownPage = null
  }

  override fun ContentDrawScope.draw() {
    val page = host.topPage
    val shown = shownPage
    if (page?.key != shown?.key) {
      if (shown != null && page != null) {
        startSlide(isBack = host.isGoingBack || page.depth < shown.depth)
      }
      host.isGoingBack = false
      shownPage = page
    }

    val layer = current ?: newLayer().also { current = it }
    layer.record { this@draw.drawContent() }

    val old = outgoing
    if (old == null) {
      drawLayer(layer)
      return
    }

    val sign = if (layoutDirection == Ltr) 1f else -1f
    val direction = if (isBack) -sign else sign
    val offset = size.width * progress.value * direction
    clipRect {
      translate(left = -offset) { drawLayer(old) }
      translate(left = size.width * direction - offset) { drawLayer(layer) }
    }
  }

  // The current layer still holds the old page's last frame, so it becomes the outgoing one
  private fun startSlide(isBack: Boolean) {
    outgoing?.let(::release)
    outgoing = current
    current = null
    this.isBack = isBack
    val slide = Animatable(0f)
    progress = slide
    coroutineScope.launch {
      slide.animateTo(1f, tween(DURATION_MILLIS, easing = FastOutSlowInEasing))
      if (progress === slide) {
        outgoing?.let(::release)
        outgoing = null
      }
    }
  }

  private fun newLayer(): GraphicsLayer = currentValueOf(LocalGraphicsContext).createGraphicsLayer()

  private fun release(layer: GraphicsLayer) =
    currentValueOf(LocalGraphicsContext).releaseGraphicsLayer(layer)

  private companion object {
    const val DURATION_MILLIS = 300
  }
}
