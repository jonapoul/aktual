/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
@file:Suppress("BooleanLiteralArgument", "UnsafeCallOnNullableType", "UnusedReceiverParameter")

package aktual.core.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AktualIcons.ArrowThickDown: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = ImageVector
      .Builder(
        name = "ArrowThickDown",
        defaultWidth = 20.dp,
        defaultHeight = 20.dp,
        viewportWidth = 20f,
        viewportHeight = 20f,
      ).apply {
        path(fill = SolidColor(Color.Black)) {
          moveTo(7f, 10f)
          verticalLineTo(2f)
          horizontalLineToRelative(6f)
          verticalLineToRelative(8f)
          horizontalLineToRelative(5f)
          lineToRelative(-8f, 8f)
          lineToRelative(-8f, -8f)
          horizontalLineToRelative(5f)
          close()
        }
      }.build()

    return icon!!
  }

private var icon: ImageVector? = null
