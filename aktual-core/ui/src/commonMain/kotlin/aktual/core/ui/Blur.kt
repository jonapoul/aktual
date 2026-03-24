package aktual.core.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.theme.isLight
import alakazam.compose.VerticalSpacer
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
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
fun Modifier.blurred(orElse: Theme.() -> Color = { pageBackground }): Modifier =
  blurred(LocalHazeState.current, orElse)

@Composable
fun Modifier.blurred(state: HazeState, orElse: Theme.() -> Color = { pageBackground }): Modifier {
  val config = LocalBlurConfig.current
  val theme = LocalTheme.current

  return if (config.blurAppBars) {
    val style =
      remember(config, theme) {
        val tintAlpha: Float = if (theme.isLight()) 1f - config.blurAlpha else config.blurAlpha
        HazeStyle(
          blurRadius = config.blurRadius,
          backgroundColor = theme.cardBackground,
          tint = HazeTint(theme.cardBackground.copy(alpha = tintAlpha)),
        )
      }
    hazeEffect(state, style)
  } else {
    background(theme.orElse())
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

@Composable
fun Modifier.blurredTopBar(state: BlurredTopBarState): Modifier = blurred(state.hazeState)

/** Variant that animates between transparent and blurred based on [isScrolled]. */
@Composable
fun Modifier.blurredTopBar(state: BlurredTopBarState, isScrolled: Boolean): Modifier {
  val config = LocalBlurConfig.current
  val theme = LocalTheme.current

  if (!state.blurEnabled) return this

  val progress by animateFloatAsState(if (isScrolled) 1f else 0f)
  if (progress == 0f) return this

  val style =
    remember(config, theme, progress) {
      val tintAlpha = if (theme.isLight()) 1f - config.blurAlpha else config.blurAlpha
      HazeStyle(
        blurRadius = config.blurRadius * progress,
        backgroundColor = theme.cardBackground.copy(alpha = progress),
        tint = HazeTint(theme.cardBackground.copy(alpha = tintAlpha * progress)),
      )
    }
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

  val target = if (dialogBlurState.isActive && blurConfig.blurDialogs) 1f else 0f
  val progress by animateFloatAsState(target)

  if (progress > 0f) {
    val style =
      remember(blurConfig, theme, progress) {
        HazeStyle(
          blurRadius = blurConfig.blurRadius * progress,
          backgroundColor = theme.cardBackground.copy(alpha = progress * 0.1f),
          tint = HazeTint(theme.cardBackground.copy(alpha = blurConfig.blurAlpha * progress)),
        )
      }
    Box(modifier = modifier.fillMaxSize().hazeEffect(hazeState, style))
  }
}

@Immutable
data class BlurConfig(
  val blurAppBars: Boolean = true,
  val blurDialogs: Boolean = true,
  val blurRadius: Dp = 5.dp,
  val blurAlpha: Float = 0.5f,
)
