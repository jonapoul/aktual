package aktual.core.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
actual fun isMobileLandscape(): Boolean {
  val configuration = LocalConfiguration.current
  return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
