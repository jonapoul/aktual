@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path

val AktualIcons.Delete: ImageVector by lazy {
  aktualIcon(name = "Delete", size = 36f) {
    group(translationX = 6f, translationY = 6f) {
      strokePath {
        moveTo(2f, 2f)
        lineTo(22f, 22f)
      }
      strokePath {
        moveTo(22f, 2f)
        lineTo(2f, 22f)
      }
    }
  }
}

private inline fun ImageVector.Builder.strokePath(pathBuilder: PathBuilder.() -> Unit) =
  path(
    stroke = SolidColor(Black),
    strokeLineWidth = 4f,
    strokeLineCap = Round,
    strokeLineMiter = 10f,
    pathBuilder = pathBuilder,
  )
