package aktual.core.ui

import aktual.core.l10n.Drawables
import aktual.core.ui.AktualTheme.colors
import aktual.theme.Colors
import aktual.theme.CustomColors
import aktual.theme.DarkColors
import aktual.theme.LightColors
import aktual.theme.LightSidebarColors
import aktual.theme.MidnightColors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
fun WavyBackground(modifier: Modifier = Modifier) =
  Image(
    modifier = modifier.fillMaxSize(),
    painter = backgroundImage(colors),
    contentDescription = null,
    contentScale = Crop,
  )

@Composable
fun PageBackground(modifier: Modifier = Modifier) =
  Box(modifier = modifier.fillMaxSize().background(colors.backgroundPage))

@Composable
private fun backgroundImage(colors: Colors) =
  when (colors) {
    LightColors,
    LightSidebarColors -> Drawables.wavyBackgroundLight
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
private fun PreviewWavyBackground(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { WavyBackground() }
