package aktual.core.ui

import aktual.core.theme.BottomBarThemeAttrs
import aktual.core.theme.Colors
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun Modifier.blurredBottomBar(
  attrs: BottomBarThemeAttrs = LocalBottomBarThemeAttrs.current.current,
  state: HazeState = LocalHazeState.current,
  config: BlurConfig = LocalBlurConfig.current,
): Modifier {
  val color = attrs.background(colors)

  return if (config.blurAppBars) {
    val blurStyle =
      remember(config, color) {
        HazeBlurStyle(
          blurRadius = config.blurRadius,
          backgroundColor = color,
          colorEffect = HazeColorEffect.tint(color.copy(alpha = config.blurAlpha)),
        )
      }
    hazeEffect(state) { blurEffect { style = blurStyle } }
  } else {
    // when blur is off, fall back to a flat fill with the same color, so the bar still matches
    // what the blurred variant would have shown
    background(color)
  }
}

/**
 * Creates a [BlurredTopBarState] for use with a transparent, blurred TopAppBar. When blur is
 * enabled, the content scrolls behind the TopAppBar with a blur effect. When disabled, normal
 * Scaffold padding behavior is used.
 */
@Composable
fun rememberBlurredTopBarState(): BlurredTopBarState {
  val hazeState = rememberHazeState()
  val blurEnabled = LocalBlurConfig.current.blurAppBars
  return remember(hazeState, blurEnabled) { BlurredTopBarState(hazeState, blurEnabled) }
}

@Stable data class BlurredTopBarState(val hazeState: HazeState, val blurEnabled: Boolean)

/** Variant that animates between transparent and blurred based on [isScrolled]. */
@Composable
fun Modifier.blurredTopBar(
  state: BlurredTopBarState,
  isScrolled: Boolean,
  config: BlurConfig = LocalBlurConfig.current,
): Modifier {
  if (!state.blurEnabled) return this

  val progress by
    animateFloatAsState(
      targetValue = if (isScrolled) 1f else 0f,
      animationSpec = DefaultAnimationSpec,
    )

  if (progress == 0f) return this

  val blurStyle = rememberAnimatedHazeStyle(config, progress)
  return hazeEffect(state.hazeState) { blurEffect { style = blurStyle } }
}

@Composable
fun Modifier.blurredTopBarContent(
  state: BlurredTopBarState,
  innerPadding: PaddingValues,
): Modifier {
  val layoutDirection = LocalLayoutDirection.current
  return if (state.blurEnabled) {
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

fun blurredTopBarContentPadding(
  state: BlurredTopBarState,
  innerPadding: PaddingValues,
): PaddingValues =
  if (state.blurEnabled) {
    PaddingValues(top = innerPadding.calculateTopPadding())
  } else {
    PaddingValues()
  }

/** Spacer that fills the height of the blurred TopAppBar, or emits nothing when blur is off. */
@Composable
fun BlurredTopBarSpacing(state: BlurredTopBarState, innerPadding: PaddingValues) {
  if (state.blurEnabled) {
    VerticalSpacer(innerPadding.calculateTopPadding())
  }
}

/**
 * Full-screen blur overlay that activates when any [AktualAlertDialog] is showing. Place this in
 * the root layout on top of the main content but below the dialog window.
 */
@Composable
fun DialogBlurOverlay(modifier: Modifier = Modifier) {
  val blurConfig = LocalBlurConfig.current
  val dialogBlurState = LocalDialogBlurState.current
  val hazeState = LocalHazeState.current

  val progress by
    animateFloatAsState(
      targetValue = if (dialogBlurState.isActive && blurConfig.blurDialogs) 1f else 0f,
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
    val blurStyle = rememberAnimatedHazeStyle(blurConfig, progress)
    val excluded = dialogBlurState.excludedFromBlur
    Box(
      modifier =
        modifier
          .fillMaxSize()
          .then(if (excluded.isEmpty()) Modifier else Modifier.clip(HoledShape(excluded.values)))
          .hazeEffect(hazeState) { blurEffect { style = blurStyle } }
    )
  }
}

// Shape covering the full composable area minus rectangular holes, used to punch the blur
// overlay out from behind expanded dropdown anchors so they appear unblurred.
private class HoledShape(private val holes: Collection<Rect>) : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val path =
      Path().apply {
        addRect(Rect(Offset.Zero, size))
        fillType = PathFillType.EvenOdd
        holes.forEach { addRect(it) }
      }
    return Outline.Generic(path)
  }
}

private val DefaultAnimationSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

@Composable
private fun rememberAnimatedHazeStyle(
  config: BlurConfig,
  progress: Float,
  colors: Colors = AktualTheme.colors,
  backgroundAlpha: Float = progress,
  tintAlpha: Float = config.blurAlpha * progress,
): HazeBlurStyle =
  remember(config, colors, progress, backgroundAlpha, tintAlpha) {
    HazeBlurStyle(
      blurRadius = config.blurRadius * progress,
      backgroundColor = colors.cardBackground.copy(alpha = backgroundAlpha),
      colorEffect = HazeColorEffect.tint(colors.cardBackground.copy(alpha = tintAlpha)),
    )
  }

@Immutable
data class BlurConfig(
  val blurAppBars: Boolean = true,
  val blurDialogs: Boolean = true,
  val blurRadius: Dp = 5.dp,
  val blurAlpha: Float = 0.5f,
)
