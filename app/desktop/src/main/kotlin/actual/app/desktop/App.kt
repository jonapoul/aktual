package actual.app.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main(): Unit = singleWindowApplication(
  title = "Widgets Gallery",
  icon = null, // TBC
  resizable = true,
  enabled = true,
  focusable = true,
  exitProcessOnExit = true,
  state = WindowState(
    size = DpSize(800.dp, 800.dp),
    placement = WindowPlacement.Floating,
    position = WindowPosition.PlatformDefault,
    isMinimized = false,
  ),
) {
  MainView()
}

@Composable
private fun MainView(modifier: Modifier = Modifier) = Box(
  modifier = modifier
    .fillMaxSize()
    .background(Color.Red),
  contentAlignment = Alignment.Center,
) {
  Text(
    text = "Hello world",
  )
}
