package aktual.core.ui

import aktual.theme.Colors
import aktual.theme.LocalColors
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.shimmerSpec
import kotlinx.collections.immutable.persistentListOf

object AktualTheme {
  @get:Composable
  @get:ReadOnlyComposable
  val colors: Colors
    get() = LocalColors.current

  @get:Composable
  @get:ReadOnlyComposable
  val typography: Typography
    get() = MaterialTheme.typography

  val textSizes: TextSizes = TextSizes

  val lineHeights: LineHeights = LineHeights

  val letterSpacings: LetterSpacings = LetterSpacings
}

@Composable
@Suppress("ModifierMissing")
fun AktualTheme(colors: Colors?, content: @Composable () -> Unit) {
  LoadingScreenIfNull(colors) { c ->
    CompositionLocalProvider(
      LocalColors provides c,
      LocalIndication provides ripple(),
      LocalShimmerTheme provides aktualShimmerTheme(c),
      LocalOverscrollFactory provides releasingOverscrollFactory(),
      LocalContentColor provides c.text,
      LocalTextSelectionColors provides
        TextSelectionColors(
          handleColor = c.accent,
          backgroundColor = c.formInputBackgroundSelection,
        ),
    ) {
      SetStatusBarColors(colors = c)

      MaterialTheme(
        colorScheme = aktualColorScheme(c),
        typography = aktualTypography(c),
        content = content,
      )
    }
  }
}

@Stable
@Composable
@Suppress("MagicNumber")
private fun aktualShimmerTheme(colors: Colors): ShimmerTheme =
  remember(colors) {
    ShimmerTheme(
      animationSpec =
        infiniteRepeatable(
          animation = shimmerSpec(durationMillis = 800, easing = LinearEasing, delayMillis = 1_500),
          repeatMode = Restart,
        ),
      blendMode = DstIn,
      rotation = 15.0f,
      shaderColors =
        persistentListOf(
          colors.text.copy(alpha = 0.1f),
          colors.text.copy(alpha = 0.3f),
          colors.text.copy(alpha = 0.1f),
        ),
      shaderColorStops = persistentListOf(0.0f, 0.5f, 1.0f),
      shimmerWidth = 400.dp,
    )
  }

@Stable
@Composable
private fun aktualColorScheme(colors: Colors): ColorScheme =
  remember(colors) {
    val base = if (colors.isLight) lightColorScheme() else darkColorScheme()
    base.copy(
      primary = colors.accent,
      onPrimary = colors.accentContrast,
      primaryContainer = colors.accentSubtle,
      onPrimaryContainer = colors.accentSubtleText,
      secondaryContainer = colors.border,
      background = colors.backgroundPage,
      onBackground = colors.text,
      surface = colors.backgroundPage,
      onSurface = colors.text,
      surfaceVariant = colors.background,
      onSurfaceVariant = colors.textTertiary,
      surfaceContainerLowest = colors.cardBackground,
      surfaceContainerLow = colors.cardBackground,
      surfaceContainer = colors.cardBackground,
      surfaceContainerHigh = colors.modalBackground,
      surfaceContainerHighest = colors.modalBackground,
      inverseSurface = colors.tooltipBackground,
      inverseOnSurface = colors.tooltipText,
      error = colors.errorText,
      errorContainer = colors.errorBackground,
      onErrorContainer = colors.errorText,
      outline = colors.formInputBorder,
      outlineVariant = colors.border,
      scrim = colors.overlayBackground,
    )
  }
