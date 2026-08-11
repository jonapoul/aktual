package aktual.core.ui

import aktual.budget.model.BarEffect
import aktual.core.theme.BottomBarThemeAttrs
import aktual.core.ui.AktualTheme.colors
import alakazam.compose.VerticalSpacer
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.glass.GlassOptics
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun Modifier.hazedBottomBar(
  attrs: BottomBarThemeAttrs = LocalBottomBarThemeAttrs.current.current,
  state: HazeState = LocalHazeState.current,
  config: HazeConfig = LocalHazeConfig.current,
): Modifier {
  val color = attrs.background(colors)
  val input = HazeInput.Sources(state)
  return when (config.appBarEffect) {
    // when the effect is off, fall back to a flat fill with the same color, so the bar still
    // matches what the blurred/glass variant would have shown
    BarEffect.None -> background(color)
    BarEffect.Blur -> hazeBlur(input = input, style = barBlurStyle(color, config))
    BarEffect.Glass -> hazeGlass(input = input, style = barGlassStyle(color, config))
  }
}

/**
 * Creates a [HazedTopBarState] for use with a transparent TopAppBar that applies the configured
 * [BarEffect]. When the effect is enabled, the content scrolls behind the TopAppBar with a
 * blur/glass effect. When disabled, normal Scaffold padding behavior is used.
 */
@Composable
fun rememberHazedTopBarState(): HazedTopBarState {
  val hazeState = rememberHazeState()
  val effect = LocalHazeConfig.current.appBarEffect
  return remember(hazeState, effect) { HazedTopBarState(hazeState, effect) }
}

@Stable
data class HazedTopBarState(val hazeState: HazeState, val effect: BarEffect) {
  val enabled: Boolean
    get() = effect != BarEffect.None
}

/**
 * Variant that scales the effect with the scroll position. Intensity ramps from fully transparent
 * to fully applied as [scrollOffset] (in pixels) grows from zero to the top bar's own measured
 * height, which is captured from the bar's layout pass.
 */
@Composable
fun Modifier.hazedTopBar(
  state: HazedTopBarState,
  scrollOffset: () -> Float,
  config: HazeConfig = LocalHazeConfig.current,
): Modifier {
  if (!state.enabled) return this

  var barHeightPx by remember { mutableIntStateOf(0) }
  val measured = onSizeChanged { barHeightPx = it.height }

  val progress by remember {
    derivedStateOf {
      val height = barHeightPx
      if (height <= 0) 0f else (scrollOffset() / height).coerceIn(0f, 1f)
    }
  }

  if (progress <= 0f) return measured

  val color = colors.cardBackground
  val input = HazeInput.Sources(state.hazeState)
  return when (state.effect) {
    BarEffect.None -> measured
    BarEffect.Blur ->
      measured.hazeBlur(input = input, style = barBlurStyle(color, config, progress))

    BarEffect.Glass ->
      measured.hazeGlass(input = input, style = barGlassStyle(color, config, progress))
  }
}

/** Convenience overload that derives the scroll offset straight from [listState]. */
@Composable
fun Modifier.hazedTopBar(
  state: HazedTopBarState,
  listState: LazyListState,
  config: HazeConfig = LocalHazeConfig.current,
): Modifier = hazedTopBar(state, scrollOffset = { listState.topBarHazeOffset() }, config = config)

// Pixels the list content has scrolled up behind the top bar. Once we're past the first item we're
// definitely fully scrolled, so report a saturating value to hold the blur at max.
fun LazyListState.topBarHazeOffset(): Float =
  if (firstVisibleItemIndex > 0) Float.MAX_VALUE else firstVisibleItemScrollOffset.toFloat()

@Composable
@ReadOnlyComposable
fun Modifier.hazedTopBarContent(state: HazedTopBarState, innerPadding: PaddingValues): Modifier {
  val layoutDirection = LocalLayoutDirection.current
  return if (state.enabled) {
    hazeSource(state.hazeState)
      .padding(
        PaddingValues(
          start = innerPadding.calculateStartPadding(layoutDirection),
          end = innerPadding.calculateEndPadding(layoutDirection),
          bottom = innerPadding.calculateBottomPadding(),
        )
      )
  } else {
    padding(innerPadding)
  }
}

fun hazedTopBarContentPadding(state: HazedTopBarState, innerPadding: PaddingValues): PaddingValues =
  if (state.enabled) {
    PaddingValues(top = innerPadding.calculateTopPadding())
  } else {
    PaddingValues()
  }

/**
 * Spacer that fills the height of the blurred TopAppBar, or emits nothing when the effect is off.
 */
@Composable
fun HazedTopBarSpacing(state: HazedTopBarState, innerPadding: PaddingValues) {
  if (state.enabled) {
    VerticalSpacer(innerPadding.calculateTopPadding())
  }
}

/**
 * Full-screen blur overlay that activates when any [AktualAlertDialog] is showing. Place this in
 * the root layout on top of the main content but below the dialog window.
 */
@Composable
fun DialogBlurOverlay(modifier: Modifier = Modifier) {
  val blurConfig = LocalHazeConfig.current
  val dialogBlurState = LocalDialogBlurState.current
  val hazeState = LocalHazeState.current

  val progress by
    animateFloatAsState(
      targetValue = if (dialogBlurState.isActive && blurConfig.dialogs) 1f else 0f,
      animationSpec = DefaultAnimationSpec,
    )

  LaunchedEffect(progress) {
    // Only clear this from the root overlay, because we only want to stop excluding these areas
    // when the blur effect
    // is totally gone
    if (progress == 0f) {
      dialogBlurState.excludedFromBlur.clear()
    }
  }

  if (progress > 0f) {
    val blurStyle = barBlurStyle(colors.cardBackground, blurConfig, progress)
    val excluded = dialogBlurState.excludedFromBlur
    Box(
      modifier =
        modifier
          .fillMaxSize()
          .then(if (excluded.isEmpty()) Modifier else Modifier.clip(HoledShape(excluded)))
          .hazeBlur(input = HazeInput.Sources(hazeState), style = blurStyle)
    )
  }
}

// Shape covering the full composable area minus rectangular holes, used to punch the blur
// overlay out from behind expanded dropdown anchors so they appear unblurred.
@Immutable
private class HoledShape(private val holes: SnapshotStateMap<Any, Rect>) : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val path =
      Path().apply {
        addRect(Rect(Offset.Zero, size))
        fillType = PathFillType.EvenOdd
        holes.values.forEach { addRect(it) }
      }
    return Outline.Generic(path)
  }
}

private val DefaultAnimationSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

// Bars are full-width rectangles, not floating cards, so glass renders with square corners.
private val BarGlassShape = RoundedCornerShape(0.dp)

@Composable
private fun barBlurStyle(color: Color, config: HazeConfig, progress: Float = 1f): HazeBlurStyle =
  remember(color, config, progress) {
    HazeBlurStyle {
      blurRadius(config.radius * progress)
      backgroundColor(color.copy(alpha = color.alpha * progress))
      colorEffects(listOf(HazeColorEffect.tint(color.copy(alpha = config.alpha * progress))))
    }
  }

@Composable
private fun barGlassStyle(color: Color, config: HazeConfig, progress: Float = 1f): GlassStyle =
  remember(color, config, progress) {
    GlassStyle {
      shape(BarGlassShape)
      edgeSoftness(0.dp)
      tint(color.copy(alpha = config.alpha * progress))
      optics(blurRadius = config.radius)
      optics(GlassOptics.Adaptive)
      alpha(progress)
    }
  }

@Immutable
data class HazeConfig(
  val appBarEffect: BarEffect = BarEffect.Default,
  val dialogs: Boolean = true,
  val radius: Dp = 5.dp,
  val alpha: Float = 0.5f,
)
