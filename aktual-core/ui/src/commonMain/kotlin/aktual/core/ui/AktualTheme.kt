package aktual.core.ui

import aktual.core.theme.Colors
import aktual.core.theme.LocalColors
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.shimmerSpec
import kotlinx.collections.immutable.persistentListOf

@Immutable
object AktualTheme {
  @get:Composable
  @get:ReadOnlyComposable
  val colors: Colors
    get() = LocalColors.current

  @get:Composable
  @get:ReadOnlyComposable
  val typography: Typography
    get() = MaterialTheme.typography
}

@Composable
@Suppress("ModifierMissing")
fun AktualTheme(colors: Colors?, content: @Composable () -> Unit) {
  LoadingScreenIfNotNull(colors) { c ->
    CompositionLocalProvider(
      LocalColors provides c,
      LocalIndication provides ripple(),
      LocalShimmerTheme provides aktualShimmerTheme(c),
      LocalOverscrollFactory provides null,
    ) {
      SetStatusBarColors(colors = c)

      MaterialTheme(
        colorScheme = if (c.isLight) lightColorScheme() else darkColorScheme(),
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
          repeatMode = RepeatMode.Restart,
        ),
      blendMode = BlendMode.DstIn,
      rotation = 15.0f,
      shaderColors =
        persistentListOf(
          colors.pageText.copy(alpha = 0.1f),
          colors.pageText.copy(alpha = 0.3f),
          colors.pageText.copy(alpha = 0.1f),
        ),
      shaderColorStops = persistentListOf(0.0f, 0.5f, 1.0f),
      shimmerWidth = 400.dp,
    )
  }
