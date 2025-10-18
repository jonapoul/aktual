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
package actual.core.icons.previews

import actual.core.icons.ActualIcons
import actual.core.icons.ArrowThickDown
import actual.core.icons.ArrowThickUp
import actual.core.icons.CloseBracket
import actual.core.icons.Cloud
import actual.core.icons.CloudCheck
import actual.core.icons.CloudDownload
import actual.core.icons.CloudUnknown
import actual.core.icons.CloudUpload
import actual.core.icons.CloudWarning
import actual.core.icons.Equals
import actual.core.icons.FileDouble
import actual.core.icons.Key
import actual.core.icons.OpenBracket
import actual.core.icons.Sum
import actual.core.ui.PreviewParameters
import actual.core.ui.PreviewThemedRow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun PreviewIcons(
  @PreviewParameter(IconPreviewParameters::class) icon: ImageVector,
) = PreviewThemedRow {
  Icon(
    imageVector = icon,
    contentDescription = null,
  )
}

private class IconPreviewParameters : PreviewParameters<ImageVector>(
  with(ActualIcons) {
    listOf(
      ArrowThickDown,
      ArrowThickUp,
      CloseBracket,
      Cloud,
      CloudCheck,
      CloudDownload,
      CloudUnknown,
      CloudUpload,
      CloudWarning,
      Equals,
      FileDouble,
      Key,
      OpenBracket,
      Sum,
    )
  },
)
