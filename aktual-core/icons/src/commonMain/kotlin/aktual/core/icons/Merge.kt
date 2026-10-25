@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path

val AktualIcons.Merge: ImageVector by lazy {
  aktualIcon(name = "Merge", size = 32f) {
    strokePath {
      moveTo(24f, 29f)
      horizontalLineTo(29.3333f)
    }
    strokePath {
      moveTo(8.00002f, 29f)
      horizontalLineTo(2.66669f)
    }
    strokePath {
      moveTo(16f, 21f)
      lineTo(8f, 29f)
    }
    strokePath {
      moveTo(16f, 21f)
      lineTo(24f, 29f)
    }
    strokePath {
      moveTo(16f, 2.66669f)
      verticalLineTo(21.3334f)
    }
    strokePath {
      moveTo(16f, 2.66669f)
      lineTo(8f, 9.33335f)
    }
    strokePath {
      moveTo(16f, 2.66669f)
      lineTo(24f, 9.33335f)
    }
  }
}

private inline fun ImageVector.Builder.strokePath(pathBuilder: PathBuilder.() -> Unit) =
  path(
    stroke = SolidColor(Black),
    strokeLineWidth = 3.5f,
    strokeLineCap = Round,
    strokeLineMiter = 10f,
    pathBuilder = pathBuilder,
  )
