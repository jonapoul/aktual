package aktual.core.icons.material.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.DefaultFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal inline fun materialIcon(
  name: String,
  viewportSize: Float = ICON_SIZE,
  autoMirror: Boolean = false,
  block: ImageVector.Builder.() -> ImageVector.Builder,
): ImageVector =
  ImageVector.Builder(
      name = "Material.$name",
      defaultWidth = ICON_SIZE.dp,
      defaultHeight = ICON_SIZE.dp,
      viewportWidth = viewportSize,
      viewportHeight = viewportSize,
      autoMirror = autoMirror,
    )
    .block()
    .build()

internal inline fun ImageVector.Builder.materialPath(
  fillAlpha: Float = 1f,
  strokeAlpha: Float = 1f,
  pathFillType: PathFillType = DefaultFillType,
  pathBuilder: PathBuilder.() -> Unit,
) =
  path(
    fill = SolidColor(Color.Black),
    fillAlpha = fillAlpha,
    stroke = null,
    strokeAlpha = strokeAlpha,
    strokeLineWidth = 1f,
    strokeLineCap = StrokeCap.Butt,
    strokeLineJoin = StrokeJoin.Bevel,
    strokeLineMiter = 1f,
    pathFillType = pathFillType,
    pathBuilder = pathBuilder,
  )

internal const val ICON_SIZE = 24f
