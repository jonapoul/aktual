package aktual.test

import aktual.theme.Colors
import aktual.theme.DarkColors
import aktual.theme.LocalColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect

fun ComposeContentTestRule.setAndroidThemedContent(
  colors: Colors = DarkColors,
  content: @Composable () -> Unit,
) = setContent {
  CompositionLocalProvider(LocalColors provides colors) {
    // Needed to initialise context for resource-fetching during tests
    CompositionLocalProvider(LocalInspectionMode provides true) {
      PreviewContextConfigurationEffect()
    }

    content()
  }
}
