package aktual.core.ui

import aktual.core.model.ColorSchemeType
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.shimmerSpec
import dev.chrisbanes.haze.LocalHazeStyle

@Composable
fun AktualTheme(type: ColorSchemeType, content: @Composable () -> Unit) {
  val theme =
    remember(type) {
      when (type) {
        ColorSchemeType.Light -> LightTheme()
        ColorSchemeType.Dark -> DarkTheme()
        ColorSchemeType.Midnight -> MidnightTheme()
      }
    }

  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalColorSchemeType provides type,
    LocalIndication provides ripple(),
    LocalShimmerTheme provides aktualLocalShimmerTheme(theme),
    LocalHazeStyle provides defaultHazeStyle(theme),
  ) {
    SetStatusBarColors(theme = theme)

    val materialColorScheme =
      when (theme) {
        is LightTheme -> lightColorScheme()
        is DarkTheme -> darkColorScheme()
        is MidnightTheme -> darkColorScheme(surface = Color.Black)
      }

    MaterialTheme(
      colorScheme = materialColorScheme,
      typography = aktualTypography(theme),
      content = content,
    )
  }
}

@Stable
@Suppress("MagicNumber")
private fun aktualLocalShimmerTheme(theme: Theme) =
  ShimmerTheme(
    animationSpec =
      infiniteRepeatable(
        animation = shimmerSpec(durationMillis = 800, easing = LinearEasing, delayMillis = 1_500),
        repeatMode = RepeatMode.Restart,
      ),
    blendMode = BlendMode.DstIn,
    rotation = 15.0f,
    shaderColors =
      listOf(
        theme.pageText.copy(alpha = 0.1f),
        theme.pageText.copy(alpha = 0.3f),
        theme.pageText.copy(alpha = 0.1f),
      ),
    shaderColorStops = listOf(0.0f, 0.5f, 1.0f),
    shimmerWidth = 400.dp,
  )
