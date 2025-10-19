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

val AktualIcons.CloudDownload: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = Builder(
      name = "CloudDownload",
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
        moveTo(24.0f, 10.663f)
        arcTo(5.817f, 5.817f, 0.0f, false, false, 22.228f, 6.5f)
        arcToRelative(5.711f, 5.711f, 0.0f, false, false, -3.447f, -1.585f)
        arcToRelative(0.249f, 0.249f, 0.0f, false, true, -0.191f, -0.12f)
        arcToRelative(7.684f, 7.684f, 0.0f, false, false, -14.1f, 2.294f)
        arcToRelative(0.251f, 0.251f, 0.0f, false, true, -0.227f, 0.2f)
        arcTo(4.59f, 4.59f, 0.0f, false, false, 0.0f, 11.859f)
        arcToRelative(4.324f, 4.324f, 0.0f, false, false, 1.236f, 3.21f)
        arcToRelative(5.529f, 5.529f, 0.0f, false, false, 3.605f, 1.377f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 0.985f, -1.015f)
        arcToRelative(1.023f, 1.023f, 0.0f, false, false, -1.015f, -0.985f)
        arcToRelative(3.3f, 3.3f, 0.0f, false, true, -2.172f, -0.8f)
        arcTo(2.374f, 2.374f, 0.0f, false, true, 2.0f, 11.859f)
        arcToRelative(2.576f, 2.576f, 0.0f, false, true, 0.954f, -2.007f)
        arcToRelative(2.6f, 2.6f, 0.0f, false, true, 2.167f, -0.527f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.2f, -0.919f)
        arcToRelative(5.686f, 5.686f, 0.0f, false, true, 10.82f, -2.088f)
        arcToRelative(0.959f, 0.959f, 0.0f, false, false, 0.941f, 0.57f)
        arcTo(3.687f, 3.687f, 0.0f, false, true, 20.84f, 7.937f)
        arcTo(3.752f, 3.752f, 0.0f, false, true, 22.0f, 10.663f)
        arcToRelative(3.835f, 3.835f, 0.0f, false, true, -3.438f, 3.791f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 0.132f, 1.992f)
        arcToRelative(0.961f, 0.961f, 0.0f, false, false, 0.131f, -0.009f)
        arcTo(5.807f, 5.807f, 0.0f, false, false, 24.0f, 10.663f)
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
        moveTo(15.5f, 17.446f)
        horizontalLineTo(13.75f)
        arcToRelative(0.25f, 0.25f, 0.0f, false, true, -0.25f, -0.25f)
        verticalLineTo(9.946f)
        arcToRelative(1.5f, 1.5f, 0.0f, true, false, -3.0f, 0.0f)
        verticalLineTo(17.2f)
        arcToRelative(0.25f, 0.25f, 0.0f, false, true, -0.25f, 0.25f)
        horizontalLineTo(8.5f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, -0.707f, 1.707f)
        lineToRelative(3.5f, 3.5f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.414f, 0.0f)
        lineToRelative(3.5f, -3.5f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, -0.707f, -1.707f)
        close()
      }
    }.build()
    return icon!!
  }

private var icon: ImageVector? = null
