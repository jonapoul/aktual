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

val AktualIcons.CloudUnknown: ImageVector
  get() {
    if (icon != null) {
      return icon!!
    }
    icon = Builder(
      name = "CloudUnknown",
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
        moveTo(3.566f, 15.457f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, -0.5f, -0.844f)
        arcToRelative(2.248f, 2.248f, 0.0f, false, true, -0.431f, -0.329f)
        arcTo(2.371f, 2.371f, 0.0f, false, true, 2.0f, 12.5f)
        arcToRelative(2.588f, 2.588f, 0.0f, false, true, 2.585f, -2.588f)
        arcToRelative(2.645f, 2.645f, 0.0f, false, true, 0.536f, 0.056f)
        arcTo(1.0f, 1.0f, 0.0f, false, false, 6.324f, 9.0f)
        curveToRelative(0.081f, -5.754f, 8.3f, -7.363f, 10.818f, -2.045f)
        arcToRelative(0.97f, 0.97f, 0.0f, false, false, 0.941f, 0.571f)
        arcToRelative(3.7f, 3.7f, 0.0f, false, true, 3.0f, 1.3f)
        arcToRelative(3.874f, 3.874f, 0.0f, false, true, 0.908f, 2.811f)
        arcToRelative(3.428f, 3.428f, 0.0f, false, true, -1.1f, 2.375f)
        arcToRelative(1.316f, 1.316f, 0.0f, false, false, -0.42f, 1.089f)
        arcToRelative(0.912f, 0.912f, 0.0f, false, false, 1.476f, 0.628f)
        arcTo(5.408f, 5.408f, 0.0f, false, false, 24.0f, 11.418f)
        arcToRelative(5.763f, 5.763f, 0.0f, false, false, -5.22f, -5.866f)
        arcToRelative(0.25f, 0.25f, 0.0f, false, true, -0.19f, -0.12f)
        arcToRelative(7.685f, 7.685f, 0.0f, false, false, -14.1f, 2.3f)
        arcToRelative(0.251f, 0.251f, 0.0f, false, true, -0.227f, 0.2f)
        arcToRelative(4.642f, 4.642f, 0.0f, false, false, -3.643f, 2.24f)
        arcTo(4.471f, 4.471f, 0.0f, false, false, 0.0f, 12.619f)
        arcToRelative(4.287f, 4.287f, 0.0f, false, false, 1.235f, 3.09f)
        arcToRelative(4.177f, 4.177f, 0.0f, false, false, 0.852f, 0.645f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, false, 1.478f, -0.9f)
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
        moveTo(18.5f, 15.8f)
        arcToRelative(6.5f, 6.5f, 0.0f, true, false, -6.5f, 6.5f)
        arcToRelative(6.508f, 6.508f, 0.0f, false, false, 6.5f, -6.5f)
        close()
        moveTo(11.25f, 17.05f)
        verticalLineToRelative(-0.55f)
        arcToRelative(1.257f, 1.257f, 0.0f, false, true, 0.986f, -1.221f)
        arcToRelative(1.125f, 1.125f, 0.0f, true, false, -1.361f, -1.1f)
        arcToRelative(0.75f, 0.75f, 0.0f, true, true, -1.5f, 0.0f)
        arcToRelative(2.625f, 2.625f, 0.0f, true, true, 3.538f, 2.461f)
        arcToRelative(0.25f, 0.25f, 0.0f, false, false, -0.163f, 0.234f)
        verticalLineToRelative(0.18f)
        arcToRelative(0.75f, 0.75f, 0.0f, false, true, -1.5f, 0.0f)
        close()
        moveTo(12.0f, 18.8f)
        arcToRelative(1.0f, 1.0f, 0.0f, true, true, -1.0f, 1.0f)
        arcToRelative(1.0f, 1.0f, 0.0f, false, true, 1.0f, -1.0f)
        close()
      }
    }.build()
    return icon!!
  }

private var icon: ImageVector? = null
