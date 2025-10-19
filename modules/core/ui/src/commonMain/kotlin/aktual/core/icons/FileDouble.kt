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
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AktualIcons.FileDouble: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = Builder(
      name = "FileDouble",
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
        moveTo(21.941f, 3.607f)
        lineTo(18.87f, 0.554f)
        arcTo(1.912f, 1.912f, 0.0f, false, false, 17.527f, 0.0f)
        horizontalLineTo(8.9f)
        arcTo(1.9f, 1.9f, 0.0f, false, false, 7.0f, 1.9f)
        verticalLineTo(5.0f)
        horizontalLineTo(3.4f)
        arcTo(1.9f, 1.9f, 0.0f, false, false, 1.5f, 6.9f)
        verticalLineToRelative(15.21f)
        arcTo(1.9f, 1.9f, 0.0f, false, false, 3.4f, 24.0f)
        horizontalLineTo(15.1f)
        arcTo(1.9f, 1.9f, 0.0f, false, false, 17.0f, 22.105f)
        verticalLineTo(19.0f)
        horizontalLineToRelative(3.6f)
        arcToRelative(1.9f, 1.9f, 0.0f, false, false, 1.9f, -1.895f)
        verticalLineTo(4.949f)
        arcTo(1.882f, 1.882f, 0.0f, false, false, 21.941f, 3.607f)
        close()
        moveTo(14.5f, 22.0f)
        horizontalLineTo(4.0f)
        arcToRelative(0.5f, 0.5f, 0.0f, false, true, -0.5f, -0.5f)
        verticalLineTo(7.5f)
        arcTo(0.5f, 0.5f, 0.0f, false, true, 4.0f, 7.0f)
        horizontalLineToRelative(7.784f)
        arcToRelative(0.5f, 0.5f, 0.0f, false, true, 0.35f, 0.143f)
        lineTo(14.85f, 9.8f)
        arcToRelative(0.5f, 0.5f, 0.0f, false, true, 0.15f, 0.357f)
        verticalLineTo(21.5f)
        arcTo(0.5f, 0.5f, 0.0f, false, true, 14.5f, 22.0f)
        close()
        moveTo(20.0f, 17.0f)
        horizontalLineTo(17.25f)
        arcToRelative(0.25f, 0.25f, 0.0f, false, true, -0.25f, -0.25f)
        verticalLineToRelative(-6.8f)
        arcToRelative(1.892f, 1.892f, 0.0f, false, false, -0.558f, -1.341f)
        lineTo(13.37f, 5.554f)
        arcTo(1.9f, 1.9f, 0.0f, false, false, 12.028f, 5.0f)
        horizontalLineTo(9.25f)
        arcTo(0.25f, 0.25f, 0.0f, false, true, 9.0f, 4.75f)
        verticalLineTo(2.5f)
        arcTo(0.5f, 0.5f, 0.0f, false, true, 9.5f, 2.0f)
        lineToRelative(7.756f, -0.026f)
        arcToRelative(0.5f, 0.5f, 0.0f, false, true, 0.351f, 0.143f)
        lineTo(20.35f, 4.8f)
        arcToRelative(0.5f, 0.5f, 0.0f, false, true, 0.15f, 0.357f)
        verticalLineTo(16.5f)
        arcTo(0.5f, 0.5f, 0.0f, false, true, 20.0f, 17.0f)
        close()
      }
    }.build()
    return icon!!
  }

private var icon: ImageVector? = null
