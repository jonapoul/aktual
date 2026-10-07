@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path

val AktualIcons.Split: ImageVector by lazy {
  aktualIcon(name = "Split", size = 32f) {
    splitStroke {
      moveTo(30f, 9f)
      lineTo(26f, 5f)
    }
    splitStroke {
      moveTo(30f, 9f)
      lineTo(26f, 13f)
    }
    splitStroke {
      moveTo(6f, 5f)
      lineTo(2f, 9f)
    }
    splitStroke {
      moveTo(2f, 9f)
      lineTo(6f, 13f)
    }
    splitStroke {
      moveTo(10f, 9f)
      horizontalLineTo(3f)
    }
    splitStroke {
      moveTo(22f, 9f)
      horizontalLineTo(29f)
    }
    splitStroke {
      moveTo(16f, 15f)
      lineTo(22f, 9f)
    }
    splitStroke {
      moveTo(16f, 15f)
      lineTo(10f, 9f)
    }
    splitStroke {
      moveTo(16f, 28f)
      lineTo(16f, 15f)
    }
  }
}

private inline fun ImageVector.Builder.splitStroke(pathBuilder: PathBuilder.() -> Unit) =
  path(
    stroke = SolidColor(Black),
    strokeLineWidth = 3.5f,
    strokeLineCap = Round,
    strokeLineMiter = 10f,
    pathBuilder = pathBuilder,
  )
