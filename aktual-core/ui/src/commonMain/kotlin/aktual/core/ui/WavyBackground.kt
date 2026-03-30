package aktual.core.ui

import aktual.core.l10n.Drawables
import aktual.core.theme.CustomTheme
import aktual.core.theme.DarkTheme
import aktual.core.theme.LightTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.MidnightTheme
import aktual.core.theme.Theme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
fun WavyBackground(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) =
  Image(
    modifier = modifier.fillMaxSize(),
    painter = backgroundImage(theme),
    contentDescription = null,
    contentScale = ContentScale.Crop,
  )

@Composable
fun PageBackground(modifier: Modifier = Modifier, theme: Theme = LocalTheme.current) =
  Box(modifier = modifier.fillMaxSize().background(theme.pageBackground))

@Composable
private fun backgroundImage(theme: Theme) =
  when (theme) {
    LightTheme -> Drawables.wavyBackgroundLight
    DarkTheme -> Drawables.wavyBackgroundDark
    MidnightTheme -> Drawables.wavyBackgroundMidnight
    is CustomTheme ->
      if (theme.isLight) {
        Drawables.wavyBackgroundLight
      } else {
        Drawables.wavyBackgroundDark
      }
  }

@LandscapePreview
@PortraitPreview
@Composable
private fun PreviewWavyBackground(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  WavyBackground(theme = theme)
