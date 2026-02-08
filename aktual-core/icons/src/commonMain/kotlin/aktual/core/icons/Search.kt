@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Search: ImageVector
  get() {
    if (_search != null) {
      return requireNotNull(_search)
    }
    _search = materialIcon(name = "Filled.Search") {
      materialPath {
        moveTo(15.5f, 14.0f)
        horizontalLineToRelative(-0.79f)
        lineToRelative(-0.28f, -0.27f)
        curveTo(15.41f, 12.59f, 16.0f, 11.11f, 16.0f, 9.5f)
        curveTo(16.0f, 5.91f, 13.09f, 3.0f, 9.5f, 3.0f)
        reflectiveCurveTo(3.0f, 5.91f, 3.0f, 9.5f)
        reflectiveCurveTo(5.91f, 16.0f, 9.5f, 16.0f)
        curveToRelative(1.61f, 0.0f, 3.09f, -0.59f, 4.23f, -1.57f)
        lineToRelative(0.27f, 0.28f)
        verticalLineToRelative(0.79f)
        lineToRelative(5.0f, 4.99f)
        lineTo(20.49f, 19.0f)
        lineToRelative(-4.99f, -5.0f)
        close()
        moveTo(9.5f, 14.0f)
        curveTo(7.01f, 14.0f, 5.0f, 11.99f, 5.0f, 9.5f)
        reflectiveCurveTo(7.01f, 5.0f, 9.5f, 5.0f)
        reflectiveCurveTo(14.0f, 7.01f, 14.0f, 9.5f)
        reflectiveCurveTo(11.99f, 14.0f, 9.5f, 14.0f)
        close()
      }
    }
    return requireNotNull(_search)
  }

private var _search: ImageVector? = null
