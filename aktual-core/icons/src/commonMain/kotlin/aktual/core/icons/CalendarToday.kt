@file:Suppress("ObjectPropertyName", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.CalendarToday: ImageVector
  get() {
    if (_calendarToday != null) {
      return requireNotNull(_calendarToday)
    }
    _calendarToday = materialIcon(name = "Filled.CalendarToday") {
      materialPath {
        moveTo(20.0f, 3.0f)
        horizontalLineToRelative(-1.0f)
        lineTo(19.0f, 1.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(2.0f)
        lineTo(7.0f, 3.0f)
        lineTo(7.0f, 1.0f)
        lineTo(5.0f, 1.0f)
        verticalLineToRelative(2.0f)
        lineTo(4.0f, 3.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        verticalLineToRelative(16.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(16.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        lineTo(22.0f, 5.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        close()
        moveTo(20.0f, 21.0f)
        lineTo(4.0f, 21.0f)
        lineTo(4.0f, 8.0f)
        horizontalLineToRelative(16.0f)
        verticalLineToRelative(13.0f)
        close()
      }
    }
    return requireNotNull(_calendarToday)
  }

private var _calendarToday: ImageVector? = null
