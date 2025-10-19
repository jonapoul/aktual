/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
