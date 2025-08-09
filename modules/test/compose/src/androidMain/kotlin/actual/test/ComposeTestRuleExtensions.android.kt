package actual.test

import actual.core.model.ColorSchemeType
import actual.core.ui.LocalColorSchemeType
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewContextConfigurationEffect
import actual.core.ui.Theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.ComposeContentTestRule

fun ComposeContentTestRule.setAndroidThemedContent(
  theme: Theme = Theme.dark(),
  type: ColorSchemeType = ColorSchemeType.Dark,
  content: @Composable () -> Unit,
) = setContent {
  CompositionLocalProvider(
    LocalTheme provides theme,
    LocalColorSchemeType provides type,
  ) {
    // Needed to initialise context for resource-fetching during tests
    PreviewContextConfigurationEffect()

    content()
  }
}
