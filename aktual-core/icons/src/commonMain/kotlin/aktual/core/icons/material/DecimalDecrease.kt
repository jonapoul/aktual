@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.DecimalDecrease: ImageVector by lazy {
  materialIcon(name = "DecimalDecrease", viewportSize = 960f) {
    materialPath {
      moveTo(640f, 880f)
      lineTo(480f, 720f)
      lineToRelative(160f, -160f)
      lineToRelative(56f, 56f)
      lineToRelative(-63f, 64f)
      horizontalLineToRelative(247f)
      verticalLineToRelative(80f)
      horizontalLineTo(633f)
      lineToRelative(63f, 64f)
      lineToRelative(-56f, 56f)
      close()
      moveTo(80f, 520f)
      verticalLineToRelative(-120f)
      horizontalLineToRelative(120f)
      verticalLineToRelative(120f)
      horizontalLineTo(80f)
      close()
      moveToRelative(201f, -41f)
      quadToRelative(-41f, -41f, -41f, -99f)
      verticalLineToRelative(-160f)
      quadToRelative(0f, -58f, 41f, -99f)
      reflectiveQuadToRelative(99f, -41f)
      quadToRelative(58f, 0f, 99f, 41f)
      reflectiveQuadToRelative(41f, 99f)
      verticalLineToRelative(160f)
      quadToRelative(0f, 58f, -41f, 99f)
      reflectiveQuadToRelative(-99f, 41f)
      quadToRelative(-58f, 0f, -99f, -41f)
      close()
      moveToRelative(141.5f, -56.5f)
      quadTo(440f, 405f, 440f, 380f)
      verticalLineToRelative(-160f)
      quadToRelative(0f, -25f, -17.5f, -42.5f)
      reflectiveQuadTo(380f, 160f)
      quadToRelative(-25f, 0f, -42.5f, 17.5f)
      reflectiveQuadTo(320f, 220f)
      verticalLineToRelative(160f)
      quadToRelative(0f, 25f, 17.5f, 42.5f)
      reflectiveQuadTo(380f, 440f)
      quadToRelative(25f, 0f, 42.5f, -17.5f)
      close()
    }
  }
}
