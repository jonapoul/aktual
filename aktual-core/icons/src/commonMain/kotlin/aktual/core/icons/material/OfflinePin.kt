@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.OfflinePin: ImageVector by lazy {
  materialIcon(name = "OfflinePin", viewportSize = 960f) {
    materialPath {
      moveTo(320f, 680f)
      horizontalLineToRelative(320f)
      verticalLineToRelative(-80f)
      horizontalLineTo(320f)
      verticalLineToRelative(80f)
      close()
      moveToRelative(118f, -120f)
      lineToRelative(226f, -226f)
      lineToRelative(-57f, -55f)
      lineToRelative(-169f, 169f)
      lineToRelative(-86f, -86f)
      lineToRelative(-56f, 56f)
      lineToRelative(142f, 142f)
      close()
      moveToRelative(42f, 320f)
      quadToRelative(-83f, 0f, -156f, -31.5f)
      reflectiveQuadTo(197f, 763f)
      quadToRelative(-54f, -54f, -85.5f, -127f)
      reflectiveQuadTo(80f, 480f)
      quadToRelative(0f, -83f, 31.5f, -156f)
      reflectiveQuadTo(197f, 197f)
      quadToRelative(54f, -54f, 127f, -85.5f)
      reflectiveQuadTo(480f, 80f)
      quadToRelative(83f, 0f, 156f, 31.5f)
      reflectiveQuadTo(763f, 197f)
      quadToRelative(54f, 54f, 85.5f, 127f)
      reflectiveQuadTo(880f, 480f)
      quadToRelative(0f, 83f, -31.5f, 156f)
      reflectiveQuadTo(763f, 763f)
      quadToRelative(-54f, 54f, -127f, 85.5f)
      reflectiveQuadTo(480f, 880f)
      close()
    }
  }
}
