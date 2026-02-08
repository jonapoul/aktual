@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.DeleteForever: ImageVector
  get() {
    if (_deleteForever != null) {
      return _deleteForever!!
    }
    _deleteForever = materialIcon(name = "Filled.DeleteForever") {
      materialPath {
        moveTo(6.0f, 19.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(8.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        lineTo(18.0f, 7.0f)
        lineTo(6.0f, 7.0f)
        verticalLineToRelative(12.0f)
        close()
        moveTo(8.46f, 11.88f)
        lineToRelative(1.41f, -1.41f)
        lineTo(12.0f, 12.59f)
        lineToRelative(2.12f, -2.12f)
        lineToRelative(1.41f, 1.41f)
        lineTo(13.41f, 14.0f)
        lineToRelative(2.12f, 2.12f)
        lineToRelative(-1.41f, 1.41f)
        lineTo(12.0f, 15.41f)
        lineToRelative(-2.12f, 2.12f)
        lineToRelative(-1.41f, -1.41f)
        lineTo(10.59f, 14.0f)
        lineToRelative(-2.13f, -2.12f)
        close()
        moveTo(15.5f, 4.0f)
        lineToRelative(-1.0f, -1.0f)
        horizontalLineToRelative(-5.0f)
        lineToRelative(-1.0f, 1.0f)
        lineTo(5.0f, 4.0f)
        verticalLineToRelative(2.0f)
        horizontalLineToRelative(14.0f)
        lineTo(19.0f, 4.0f)
        close()
      }
    }
    return _deleteForever!!
  }

private var _deleteForever: ImageVector? = null
