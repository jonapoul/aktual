@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Speed125: ImageVector by lazy {
  materialIcon(name = "Speed125", viewportSize = 960f) {
    materialPath {
      // "1"
      moveTo(262f, 683f)
      verticalLineToRelative(-60f)
      horizontalLineToRelative(60f)
      verticalLineToRelative(60f)
      horizontalLineToRelative(-60f)
      close()
      // "25" right portion
      moveToRelative(408f, 0f)
      verticalLineToRelative(-60f)
      horizontalLineToRelative(170f)
      verticalLineToRelative(-115f)
      horizontalLineTo(670f)
      verticalLineToRelative(-231f)
      horizontalLineToRelative(230f)
      verticalLineToRelative(60f)
      horizontalLineTo(730f)
      verticalLineToRelative(111f)
      horizontalLineToRelative(110f)
      quadToRelative(24f, 0f, 42f, 18f)
      reflectiveQuadToRelative(18f, 42f)
      verticalLineToRelative(115f)
      quadToRelative(0f, 24f, -18f, 42f)
      reflectiveQuadToRelative(-42f, 18f)
      horizontalLineTo(670f)
      close()
      // "2" portion
      moveToRelative(-289f, 0f)
      verticalLineToRelative(-175f)
      quadToRelative(0f, -24f, 18f, -42f)
      reflectiveQuadToRelative(42f, -18f)
      horizontalLineToRelative(110f)
      verticalLineToRelative(-111f)
      horizontalLineTo(381f)
      verticalLineToRelative(-60f)
      horizontalLineToRelative(170f)
      quadToRelative(24f, 0f, 42f, 18f)
      reflectiveQuadToRelative(18f, 42f)
      verticalLineToRelative(111f)
      quadToRelative(0f, 24f, -18f, 42f)
      reflectiveQuadToRelative(-42f, 18f)
      horizontalLineTo(441f)
      verticalLineToRelative(115f)
      horizontalLineToRelative(170f)
      verticalLineToRelative(60f)
      horizontalLineTo(381f)
      close()
      // "1" left portion
      moveToRelative(-238f, 0f)
      verticalLineToRelative(-346f)
      horizontalLineTo(60f)
      verticalLineToRelative(-60f)
      horizontalLineToRelative(143f)
      verticalLineToRelative(406f)
      horizontalLineToRelative(-60f)
      close()
    }
  }
}
