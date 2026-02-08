@file:Suppress("BooleanLiteralArgument", "UnsafeCallOnNullableType", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AktualIcons.CloudWarning: ImageVector
  get() {
    if (icon != null) {
      return requireNotNull(icon)
    }
    icon = Builder(
      name = "CloudWarning",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      path(
        fill = SolidColor(Color.Black),
        stroke = null,
        strokeLineWidth = 0.0f,
        strokeLineCap = Butt,
        strokeLineJoin = Miter,
        strokeLineMiter = 4.0f,
        pathFillType = NonZero,
      ) {
        moveTo(3.924f, 17.376f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.052f, -0.534f)
        horizontalLineToRelative(0.0f)
        arcToRelative(1.018f, 1.018f, 0.0f, false, false, -0.743f, -1.476f)
        arcToRelative(2.518f, 2.518f, 0.0f, false, true, -1.594f, -0.707f)
        arcTo(2.371f, 2.371f, 0.0f, false, true, 2.0f, 12.874f)
        arcToRelative(2.588f, 2.588f, 0.0f, false, true, 2.586f, -2.587f)
        arcToRelative(2.635f, 2.635f, 0.0f, false, true, 0.535f, 0.056f)
        arcToRelative(1.008f, 1.008f, 0.0f, false, false, 0.811f, -0.186f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 0.392f, -0.779f)
        arcToRelative(4.521f, 4.521f, 0.0f, false, true, 0.961f, -3.012f)
        curveToRelative(3.357f, -3.8f, 8.227f, -2.47f, 9.856f, 0.966f)
        arcToRelative(1.024f, 1.024f, 0.0f, false, false, 0.978f, 0.57f)
        arcToRelative(3.666f, 3.666f, 0.0f, false, true, 2.721f, 1.05f)
        arcTo(3.757f, 3.757f, 0.0f, false, true, 22.0f, 11.679f)
        arcToRelative(3.9f, 3.9f, 0.0f, false, true, -2.483f, 3.61f)
        arcToRelative(0.969f, 0.969f, 0.0f, false, false, -0.586f, 1.373f)
        horizontalLineToRelative(0.0f)
        arcToRelative(0.956f, 0.956f, 0.0f, false, false, 1.162f, 0.47f)
        arcToRelative(5.6f, 5.6f, 0.0f, false, false, 3.9f, -5.2f)
        arcToRelative(5.769f, 5.769f, 0.0f, false, false, -5.215f, -6.007f)
        arcToRelative(0.252f, 0.252f, 0.0f, false, true, -0.191f, -0.12f)
        arcToRelative(7.685f, 7.685f, 0.0f, false, false, -14.1f, 2.3f)
        arcToRelative(0.251f, 0.251f, 0.0f, false, true, -0.227f, 0.2f)
        arcTo(4.642f, 4.642f, 0.0f, false, false, 0.62f, 10.547f)
        arcTo(4.482f, 4.482f, 0.0f, false, false, 0.0f, 12.994f)
        arcToRelative(4.287f, 4.287f, 0.0f, false, false, 1.235f, 3.09f)
        arcTo(5.057f, 5.057f, 0.0f, false, false, 3.924f, 17.376f)
        close()
      }
      path(
        fill = SolidColor(Color.Black),
        stroke = null,
        strokeLineWidth = 0.0f,
        strokeLineCap = Butt,
        strokeLineJoin = Miter,
        strokeLineMiter = 4.0f,
        pathFillType = NonZero,
      ) {
        moveTo(13.281f, 10.2f)
        arcToRelative(1.45f, 1.45f, 0.0f, false, false, -2.562f, 0.0f)
        lineTo(5.663f, 19.834f)
        arcToRelative(1.429f, 1.429f, 0.0f, false, false, 0.049f, 1.408f)
        arcToRelative(1.454f, 1.454f, 0.0f, false, false, 1.233f, 0.687f)
        lineTo(17.056f, 21.929f)
        arcToRelative(1.456f, 1.456f, 0.0f, false, false, 1.233f, -0.687f)
        arcToRelative(1.431f, 1.431f, 0.0f, false, false, 0.048f, -1.408f)
        close()
        moveTo(12.0f, 20.179f)
        arcToRelative(1.0f, 1.0f, 0.0f, true, true, 1.0f, -1.0f)
        arcTo(1.0f, 1.0f, 0.0f, false, true, 12.0f, 20.179f)
        close()
        moveTo(11.25f, 13.429f)
        arcToRelative(0.75f, 0.75f, 0.0f, false, true, 1.5f, 0.0f)
        verticalLineToRelative(3.0f)
        arcToRelative(0.75f, 0.75f, 0.0f, false, true, -1.5f, 0.0f)
        close()
      }
    }.build()
    return requireNotNull(icon)
  }

private var icon: ImageVector? = null
