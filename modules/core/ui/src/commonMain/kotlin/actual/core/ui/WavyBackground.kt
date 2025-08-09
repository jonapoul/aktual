package actual.core.ui

import actual.core.model.ColorSchemeType
import actual.l10n.Drawables
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

val LocalColorSchemeType = compositionLocalOf<ColorSchemeType> { error("No ColorSchemeType defined!") }

@Composable
fun WavyBackground(
  modifier: Modifier = Modifier,
  schemeType: ColorSchemeType = LocalColorSchemeType.current,
) = Image(
  modifier = modifier.fillMaxSize(),
  painter = backgroundImage(schemeType),
  contentDescription = null,
  contentScale = ContentScale.FillHeight,
)

@Composable
private fun backgroundImage(type: ColorSchemeType) = when (type) {
  ColorSchemeType.Light -> Drawables.wavyBackgroundLight
  ColorSchemeType.Dark -> Drawables.wavyBackgroundDark
  ColorSchemeType.Midnight -> Drawables.wavyBackgroundMidnight
}
