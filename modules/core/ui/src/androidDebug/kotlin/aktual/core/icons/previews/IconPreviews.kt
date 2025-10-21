/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.icons.previews

import aktual.core.icons.AktualIcons
import aktual.core.icons.ArrowThickDown
import aktual.core.icons.ArrowThickUp
import aktual.core.icons.CloseBracket
import aktual.core.icons.Cloud
import aktual.core.icons.CloudCheck
import aktual.core.icons.CloudDownload
import aktual.core.icons.CloudUnknown
import aktual.core.icons.CloudUpload
import aktual.core.icons.CloudWarning
import aktual.core.icons.Equals
import aktual.core.icons.FileDouble
import aktual.core.icons.Key
import aktual.core.icons.OpenBracket
import aktual.core.icons.Sum
import aktual.core.ui.PreviewParameters
import aktual.core.ui.PreviewThemedRow
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
  with(AktualIcons) {
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
