package aktual.core.icons.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal inline fun aktualIcon(
  name: String,
  size: Float,
  block: ImageVector.Builder.() -> ImageVector.Builder,
): ImageVector = aktualIcon(name, size, size, block)

internal inline fun aktualIcon(
  name: String,
  width: Float,
  height: Float,
  block: ImageVector.Builder.() -> ImageVector.Builder,
): ImageVector =
  ImageVector.Builder("Aktual.$name", width.dp, height.dp, width, height).block().build()

internal inline fun ImageVector.Builder.aktualPath(
  fillAlpha: Float = 1f,
  strokeAlpha: Float = 1f,
  strokeLineMiter: Float = 1f,
  pathFillType: PathFillType = PathFillType.NonZero,
  pathBuilder: PathBuilder.() -> Unit,
) =
  path(
    fill = SolidColor(Color.Black),
    fillAlpha = fillAlpha,
    stroke = null,
    strokeAlpha = strokeAlpha,
    strokeLineWidth = 0f,
    strokeLineCap = StrokeCap.Butt,
    strokeLineJoin = StrokeJoin.Miter,
    strokeLineMiter = strokeLineMiter,
    pathFillType = pathFillType,
    pathBuilder = pathBuilder,
  )
