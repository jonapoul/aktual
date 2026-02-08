@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Brightness3: ImageVector
  get() {
    if (_brightness3 != null) {
      return requireNotNull(_brightness3)
    }
    _brightness3 = materialIcon(name = "Filled.Brightness3") {
      materialPath {
        moveTo(9.0f, 2.0f)
        curveToRelative(-1.05f, 0.0f, -2.05f, 0.16f, -3.0f, 0.46f)
        curveToRelative(4.06f, 1.27f, 7.0f, 5.06f, 7.0f, 9.54f)
        curveToRelative(0.0f, 4.48f, -2.94f, 8.27f, -7.0f, 9.54f)
        curveToRelative(0.95f, 0.3f, 1.95f, 0.46f, 3.0f, 0.46f)
        curveToRelative(5.52f, 0.0f, 10.0f, -4.48f, 10.0f, -10.0f)
        reflectiveCurveTo(14.52f, 2.0f, 9.0f, 2.0f)
        close()
      }
    }
    return requireNotNull(_brightness3)
  }

private var _brightness3: ImageVector? = null
