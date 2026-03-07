package aktual.test

import aktual.core.theme.DarkTheme
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect

fun ComposeContentTestRule.setAndroidThemedContent(
  theme: Theme = DarkTheme,
  content: @Composable () -> Unit,
) = setContent {
  CompositionLocalProvider(LocalTheme provides theme) {
    // Needed to initialise context for resource-fetching during tests
    CompositionLocalProvider(LocalInspectionMode provides true) {
      PreviewContextConfigurationEffect()
    }

    content()
  }
}
