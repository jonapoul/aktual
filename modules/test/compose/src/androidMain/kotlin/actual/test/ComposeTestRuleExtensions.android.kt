package actual.test

import actual.core.model.ColorSchemeType
import actual.core.ui.LocalColorSchemeType
import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.ComposeContentTestRule

// TODO: move to commonMain
fun ComposeContentTestRule.setThemedContent(
  theme: Theme = Theme.dark(),
  type: ColorSchemeType = ColorSchemeType.Dark,
  composable: @Composable () -> Unit,
) = setContent {
  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalColorSchemeType provides type,
    content = composable,
  )
}
