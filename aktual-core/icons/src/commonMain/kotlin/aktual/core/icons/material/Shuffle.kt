@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons.material

import aktual.core.icons.material.internal.materialIcon
import aktual.core.icons.material.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.Shuffle: ImageVector by lazy {
  materialIcon(name = "Shuffle", viewportSize = 960f) {
    materialPath {
      moveTo(560f, 800f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(104f)
      lineTo(537f, 593f)
      lineToRelative(57f, -57f)
      lineToRelative(126f, 126f)
      verticalLineToRelative(-102f)
      horizontalLineToRelative(80f)
      verticalLineToRelative(240f)
      horizontalLineTo(560f)
      close()
      moveToRelative(-344f, 0f)
      lineToRelative(-56f, -56f)
      lineToRelative(504f, -504f)
      horizontalLineTo(560f)
      verticalLineToRelative(-80f)
      horizontalLineToRelative(240f)
      verticalLineToRelative(240f)
      horizontalLineToRelative(-80f)
      verticalLineToRelative(-104f)
      lineTo(216f, 800f)
      close()
      moveToRelative(151f, -377f)
      lineTo(160f, 216f)
      lineToRelative(56f, -56f)
      lineToRelative(207f, 207f)
      lineToRelative(-56f, 56f)
      close()
    }
  }
}
