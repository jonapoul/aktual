package aktual.app.desktop

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.navigation.NavHostController
import logcat.logcat

/** Return false => pass event up to the parent. In this case that means it gets ignored entirely */
internal class KeyboardEventHandler(private val navController: NavHostController) {
  // Intercept keys before any composable handles them
  // Use for: app-level shortcuts that should always work
  fun onKeyEvent(event: KeyEvent): Boolean =
      with(event) {
        logcat.v { "onKeyEvent = ${event.string()}" }
        when {
          else -> false
        }
      }

  // Handle keys after composables have their chance
  // Use for: app-level shortcuts that shouldn't interfere with text input
  fun onPreviewKeyEvent(event: KeyEvent): Boolean =
      with(event) {
        logcat.v { "onPreviewKeyEvent = ${event.string()}" }
        when {
          isAltPressed && key == Key.DirectionLeft -> {
            logcat.d { "Navigating back" }
            navController.navigateUp()
          }

          else -> {
            false
          }
        }
      }

  private fun KeyEvent.string(): String =
      listOf(
              "key" to key,
              "type" to type,
              "isAltPressed" to if (isAltPressed) true else null,
              "isCtrlPressed" to if (isCtrlPressed) true else null,
              "isMetaPressed" to if (isMetaPressed) true else null,
              "isShiftPressed" to if (isShiftPressed) true else null,
          )
          .mapNotNull { (k, v) -> if (v == null) null else "$k=$v" }
          .joinToString(separator = ",")
}
