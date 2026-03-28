package aktual.core.ui

import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.ShimmerTheme
import com.valentinilk.shimmer.shimmerSpec

@Composable
@Suppress("ModifierMissing")
fun AktualTheme(theme: Theme?, content: @Composable () -> Unit) {
  if (theme == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No theme", textAlign = TextAlign.Center)
    }
    return
  }

  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalIndication provides ripple(),
    LocalShimmerTheme provides aktualShimmerTheme(theme),
    LocalOverscrollFactory provides null,
  ) {
    SetStatusBarColors(theme = theme)

    MaterialTheme(
      colorScheme = if (theme.isLight) lightColorScheme() else darkColorScheme(),
      typography = aktualTypography(theme),
      content = content,
    )
  }
}

@Stable
@Composable
@Suppress("MagicNumber")
private fun aktualShimmerTheme(theme: Theme) =
  remember(theme) {
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
  }
