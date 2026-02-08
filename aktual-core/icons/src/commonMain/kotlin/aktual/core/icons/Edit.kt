@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Edit: ImageVector
  get() {
    if (_edit != null) {
      return _edit!!
    }
    _edit = materialIcon(name = "Filled.Edit") {
      materialPath {
        moveTo(3.0f, 17.25f)
        verticalLineTo(21.0f)
        horizontalLineToRelative(3.75f)
        lineTo(17.81f, 9.94f)
        lineToRelative(-3.75f, -3.75f)
        lineTo(3.0f, 17.25f)
        close()
        moveTo(20.71f, 7.04f)
        curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
        lineToRelative(-2.34f, -2.34f)
        curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
        lineToRelative(-1.83f, 1.83f)
        lineToRelative(3.75f, 3.75f)
        lineToRelative(1.83f, -1.83f)
        close()
      }
    }
    return _edit!!
  }

private var _edit: ImageVector? = null
