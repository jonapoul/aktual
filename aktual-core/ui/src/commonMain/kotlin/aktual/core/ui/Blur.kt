package aktual.core.ui

import aktual.core.theme.BottomBarThemeAttrs
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun Modifier.blurredBottomBar(
  attrs: BottomBarThemeAttrs = LocalBottomBarThemeAttrs.current.current,
  state: HazeState = LocalHazeState.current,
): Modifier {
  val config = LocalBlurConfig.current
  val theme = LocalTheme.current
  val color = attrs.background(theme)

  return if (config.blurAppBars) {
    val style =
      remember(config, theme, color) {
        val tintAlpha: Float = if (theme.isLight) 1f - config.blurAlpha else config.blurAlpha
        HazeStyle(
          blurRadius = config.blurRadius,
          backgroundColor = color,
          tint = HazeTint(color.copy(alpha = tintAlpha)),
        )
      }
    hazeEffect(state, style)
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
fun Modifier.blurredTopBar(state: BlurredTopBarState, isScrolled: Boolean): Modifier {
  val config = LocalBlurConfig.current
  val theme = LocalTheme.current

  if (!state.blurEnabled) return this

  val progress by
    animateFloatAsState(
      targetValue = if (isScrolled) 1f else 0f,
      animationSpec = DefaultAnimationSpec,
    )

  if (progress == 0f) return this

  val style = rememberAnimatedHazeStyle(config, theme, progress)
  return hazeEffect(state.hazeState, style)
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
 * Full-screen blur overlay that activates when any [AlertDialog] is showing. Place this in the root
 * layout on top of the main content but below the dialog window.
 */
@Composable
fun DialogBlurOverlay(modifier: Modifier = Modifier) {
  val blurConfig = LocalBlurConfig.current
  val dialogBlurState = LocalDialogBlurState.current
  val hazeState = LocalHazeState.current
  val theme = LocalTheme.current

  val progress by
    animateFloatAsState(
      targetValue = if (dialogBlurState.isActive && blurConfig.blurDialogs) 1f else 0f,
      animationSpec = DefaultAnimationSpec,
    )

  if (progress > 0f) {
    val style = rememberAnimatedHazeStyle(blurConfig, theme, progress)
    Box(modifier = modifier.fillMaxSize().hazeEffect(hazeState, style))
  }
}

private val DefaultAnimationSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

@Composable
private fun rememberAnimatedHazeStyle(
  config: BlurConfig,
  theme: Theme,
  progress: Float,
  backgroundAlpha: Float = progress,
  tintAlpha: Float = config.blurAlpha * progress,
): HazeStyle =
  remember(config, theme, progress, backgroundAlpha, tintAlpha) {
    HazeStyle(
      blurRadius = config.blurRadius * progress,
      backgroundColor = theme.cardBackground.copy(alpha = backgroundAlpha),
      tint = HazeTint(theme.cardBackground.copy(alpha = tintAlpha)),
    )
  }

@Immutable
data class BlurConfig(
  val blurAppBars: Boolean = true,
  val blurDialogs: Boolean = true,
  val blurRadius: Dp = 5.dp,
  val blurAlpha: Float = 0.5f,
)
