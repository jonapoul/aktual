@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Block: ImageVector
  get() {
    if (_block != null) {
      return _block!!
    }
    _block = materialIcon(name = "Filled.Block") {
      materialPath {
        moveTo(12.0f, 2.0f)
        curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
        reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
        reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
        reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
        close()
        moveTo(4.0f, 12.0f)
        curveToRelative(0.0f, -4.42f, 3.58f, -8.0f, 8.0f, -8.0f)
        curveToRelative(1.85f, 0.0f, 3.55f, 0.63f, 4.9f, 1.69f)
        lineTo(5.69f, 16.9f)
        curveTo(4.63f, 15.55f, 4.0f, 13.85f, 4.0f, 12.0f)
        close()
        moveTo(12.0f, 20.0f)
        curveToRelative(-1.85f, 0.0f, -3.55f, -0.63f, -4.9f, -1.69f)
        lineTo(18.31f, 7.1f)
        curveTo(19.37f, 8.45f, 20.0f, 10.15f, 20.0f, 12.0f)
        curveToRelative(0.0f, 4.42f, -3.58f, 8.0f, -8.0f, 8.0f)
        close()
      }
    }
    return _block!!
  }

private var _block: ImageVector? = null
