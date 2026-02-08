@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Apps: ImageVector
  get() {
    if (_apps != null) {
      return _apps!!
    }
    _apps = materialIcon(name = "Filled.Apps") {
      materialPath {
        moveTo(4.0f, 8.0f)
        horizontalLineToRelative(4.0f)
        lineTo(8.0f, 4.0f)
        lineTo(4.0f, 4.0f)
        verticalLineToRelative(4.0f)
        close()
        moveTo(10.0f, 20.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(-4.0f)
        horizontalLineToRelative(-4.0f)
        verticalLineToRelative(4.0f)
        close()
        moveTo(4.0f, 20.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(-4.0f)
        lineTo(4.0f, 16.0f)
        verticalLineToRelative(4.0f)
        close()
        moveTo(4.0f, 14.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(-4.0f)
        lineTo(4.0f, 10.0f)
        verticalLineToRelative(4.0f)
        close()
        moveTo(10.0f, 14.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(-4.0f)
        horizontalLineToRelative(-4.0f)
        verticalLineToRelative(4.0f)
        close()
        moveTo(16.0f, 4.0f)
        verticalLineToRelative(4.0f)
        horizontalLineToRelative(4.0f)
        lineTo(20.0f, 4.0f)
        horizontalLineToRelative(-4.0f)
        close()
        moveTo(10.0f, 8.0f)
        horizontalLineToRelative(4.0f)
        lineTo(14.0f, 4.0f)
        horizontalLineToRelative(-4.0f)
        verticalLineToRelative(4.0f)
        close()
        moveTo(16.0f, 14.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(-4.0f)
        horizontalLineToRelative(-4.0f)
        verticalLineToRelative(4.0f)
        close()
        moveTo(16.0f, 20.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(-4.0f)
        horizontalLineToRelative(-4.0f)
        verticalLineToRelative(4.0f)
        close()
      }
    }
    return _apps!!
  }

private var _apps: ImageVector? = null
