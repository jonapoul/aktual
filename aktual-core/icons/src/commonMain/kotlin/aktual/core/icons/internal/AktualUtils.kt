package aktual.core.icons.internal

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// padded adds a margin of a tenth of size on each side, matching Material's 2dp keyline at 24dp
internal inline fun aktualIcon(
  name: String,
  size: Float,
  padded: Boolean = false,
  block: ImageVector.Builder.() -> ImageVector.Builder,
): ImageVector {
  val padding = if (padded) size / 10f else 0f
  val viewport = size + 2 * padding
  return ImageVector.Builder("Aktual.$name", 24.dp, 24.dp, viewport, viewport)
    .addGroup(translationX = padding, translationY = padding)
    .block()
    .clearGroup()
    .build()
}

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
  pathFillType: PathFillType = NonZero,
  pathBuilder: PathBuilder.() -> Unit,
) =
  path(
    fill = SolidColor(Black),
    fillAlpha = fillAlpha,
    stroke = null,
    strokeAlpha = strokeAlpha,
    strokeLineWidth = 0f,
    strokeLineCap = Butt,
    strokeLineJoin = Miter,
    strokeLineMiter = strokeLineMiter,
    pathFillType = pathFillType,
    pathBuilder = pathBuilder,
  )
