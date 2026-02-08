@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Timer: ImageVector
  get() {
    if (_timer != null) {
      return requireNotNull(_timer)
    }
    _timer = materialIcon(name = "Filled.Timer") {
      materialPath {
        moveTo(9.0f, 1.0f)
        horizontalLineToRelative(6.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(-6.0f)
        close()
      }
      materialPath {
        moveTo(19.03f, 7.39f)
        lineToRelative(1.42f, -1.42f)
        curveToRelative(-0.43f, -0.51f, -0.9f, -0.99f, -1.41f, -1.41f)
        lineToRelative(-1.42f, 1.42f)
        curveTo(16.07f, 4.74f, 14.12f, 4.0f, 12.0f, 4.0f)
        curveToRelative(-4.97f, 0.0f, -9.0f, 4.03f, -9.0f, 9.0f)
        curveToRelative(0.0f, 4.97f, 4.02f, 9.0f, 9.0f, 9.0f)
        reflectiveCurveToRelative(9.0f, -4.03f, 9.0f, -9.0f)
        curveTo(21.0f, 10.88f, 20.26f, 8.93f, 19.03f, 7.39f)
        close()
        moveTo(13.0f, 14.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineTo(8.0f)
        horizontalLineToRelative(2.0f)
        verticalLineTo(14.0f)
        close()
      }
    }
    return requireNotNull(_timer)
  }

private var _timer: ImageVector? = null
