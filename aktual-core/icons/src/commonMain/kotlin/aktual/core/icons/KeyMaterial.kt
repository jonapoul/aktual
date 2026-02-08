@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Key: ImageVector
  get() {
    if (_key != null) {
      return requireNotNull(_key)
    }
    _key = materialIcon(name = "Filled.Key") {
      materialPath {
        moveTo(21.0f, 10.0f)
        horizontalLineToRelative(-8.35f)
        curveTo(11.83f, 7.67f, 9.61f, 6.0f, 7.0f, 6.0f)
        curveToRelative(-3.31f, 0.0f, -6.0f, 2.69f, -6.0f, 6.0f)
        reflectiveCurveToRelative(2.69f, 6.0f, 6.0f, 6.0f)
        curveToRelative(2.61f, 0.0f, 4.83f, -1.67f, 5.65f, -4.0f)
        horizontalLineTo(13.0f)
        lineToRelative(2.0f, 2.0f)
        lineToRelative(2.0f, -2.0f)
        lineToRelative(2.0f, 2.0f)
        lineToRelative(4.0f, -4.04f)
        lineTo(21.0f, 10.0f)
        close()
        moveTo(7.0f, 15.0f)
        curveToRelative(-1.65f, 0.0f, -3.0f, -1.35f, -3.0f, -3.0f)
        curveToRelative(0.0f, -1.65f, 1.35f, -3.0f, 3.0f, -3.0f)
        reflectiveCurveToRelative(3.0f, 1.35f, 3.0f, 3.0f)
        curveTo(10.0f, 13.65f, 8.65f, 15.0f, 7.0f, 15.0f)
        close()
      }
    }
    return requireNotNull(_key)
  }

private var _key: ImageVector? = null
