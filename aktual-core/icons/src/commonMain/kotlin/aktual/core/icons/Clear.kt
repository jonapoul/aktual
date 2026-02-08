@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Clear: ImageVector
  get() {
    if (_clear != null) {
      return requireNotNull(_clear)
    }
    _clear = materialIcon(name = "Filled.Clear") {
      materialPath {
        moveTo(19.0f, 6.41f)
        lineTo(17.59f, 5.0f)
        lineTo(12.0f, 10.59f)
        lineTo(6.41f, 5.0f)
        lineTo(5.0f, 6.41f)
        lineTo(10.59f, 12.0f)
        lineTo(5.0f, 17.59f)
        lineTo(6.41f, 19.0f)
        lineTo(12.0f, 13.41f)
        lineTo(17.59f, 19.0f)
        lineTo(19.0f, 17.59f)
        lineTo(13.41f, 12.0f)
        close()
      }
    }
    return requireNotNull(_clear)
  }

private var _clear: ImageVector? = null
