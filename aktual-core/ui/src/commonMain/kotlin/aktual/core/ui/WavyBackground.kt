package aktual.core.ui

import aktual.core.l10n.Drawables
import aktual.core.theme.Colors
import aktual.core.theme.CustomColors
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.MidnightColors
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
fun WavyBackground(modifier: Modifier = Modifier) =
  Image(
    modifier = modifier.fillMaxSize(),
    painter = backgroundImage(colors),
    contentDescription = null,
    contentScale = ContentScale.Crop,
  )

@Composable
fun PageBackground(modifier: Modifier = Modifier) =
  Box(modifier = modifier.fillMaxSize().background(colors.pageBackground))

@Composable
private fun backgroundImage(colors: Colors) =
  when (colors) {
    LightColors -> Drawables.wavyBackgroundLight
    DarkColors -> Drawables.wavyBackgroundDark
    MidnightColors -> Drawables.wavyBackgroundMidnight
    is CustomColors ->
      if (colors.isLight) {
        Drawables.wavyBackgroundLight
      } else {
        Drawables.wavyBackgroundDark
      }
  }

@LandscapePreview
@PortraitPreview
@Composable
private fun PreviewWavyBackground(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { WavyBackground() }
