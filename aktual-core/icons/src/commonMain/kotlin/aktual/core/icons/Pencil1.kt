@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.Pencil1: ImageVector by lazy {
  aktualIcon(
    name = "Pencil1",
    width = 20f,
    height = 20f,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ) {
    aktualPath {
      moveTo(15.067f, 3.986f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, -0.354f, -0.148f)
      horizontalLineToRelative(0f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, -0.354f, 0.147f)
      lineTo(3.437f, 14.91f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, 0f, 0.707f)
      lineToRelative(4.948f, 4.948f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, 0.707f, 0f)
      lineTo(20.009f, 9.648f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, 0f, -0.706f)
      close()
    }
    aktualPath {
      moveTo(2.43f, 16.8f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, -0.489f, -0.127f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, -0.351f, 0.364f)
      lineTo(0.084f, 23.314f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, 0.133f, 0.47f)
      arcToRelative(0.507f, 0.507f, 0f, false, false, 0.47f, 0.132f)
      lineToRelative(6.272f, -1.5f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, 0.237f, -0.84f)
      close()
    }
    aktualPath {
      moveTo(23.2f, 2.924f)
      lineTo(21.077f, 0.8f)
      arcToRelative(2.5f, 2.5f, 0f, false, false, -3.532f, 0f)
      lineTo(16.127f, 2.217f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, 0f, 0.707f)
      lineToRelative(4.95f, 4.949f)
      arcToRelative(0.5f, 0.5f, 0f, false, false, 0.707f, 0f)
      lineTo(23.2f, 6.454f)
      arcTo(2.5f, 2.5f, 0f, false, false, 23.2f, 2.924f)
      close()
    }
  }
}
