@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Add: ImageVector
  get() {
    if (_add != null) {
      return _add!!
    }
    _add = materialIcon(name = "Filled.Add") {
      materialPath {
        moveTo(19.0f, 13.0f)
        horizontalLineToRelative(-6.0f)
        verticalLineToRelative(6.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(-6.0f)
        horizontalLineTo(5.0f)
        verticalLineToRelative(-2.0f)
        horizontalLineToRelative(6.0f)
        verticalLineTo(5.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(6.0f)
        horizontalLineToRelative(6.0f)
        verticalLineToRelative(2.0f)
        close()
      }
    }
    return _add!!
  }

private var _add: ImageVector? = null
