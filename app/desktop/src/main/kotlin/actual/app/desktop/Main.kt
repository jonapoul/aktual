package actual.app.desktop

import actual.l10n.Drawables
import actual.l10n.Strings
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application(exitProcessOnExit = true) {
  val graphHolder = remember { DesktopAppGraphHolder() }
  Window(
    title = Strings.appName,
    icon = Drawables.appIcon48,
    resizable = true,
    enabled = true,
    focusable = true,
    state = WindowState(
      size = DpSize(800.dp, 800.dp),
      placement = WindowPlacement.Floating,
      position = WindowPosition.PlatformDefault,
      isMinimized = false,
    ),
    onCloseRequest = ::exitApplication,
  ) {
    AppLayout(
      graphHolder = graphHolder,
    )
  }
}
