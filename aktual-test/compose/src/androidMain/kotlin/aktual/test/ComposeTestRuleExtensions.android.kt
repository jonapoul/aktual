package aktual.test

import aktual.core.model.ColorSchemeType
import aktual.core.ui.LocalColorSchemeType
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect

fun ComposeContentTestRule.setAndroidThemedContent(
  theme: Theme = Theme.dark(),
  type: ColorSchemeType = ColorSchemeType.Dark,
  content: @Composable () -> Unit,
) = setContent {
  CompositionLocalProvider(LocalTheme provides theme, LocalColorSchemeType provides type) {
    // Needed to initialise context for resource-fetching during tests
    CompositionLocalProvider(LocalInspectionMode provides true) {
      PreviewContextConfigurationEffect()
    }

    content()
  }
}
