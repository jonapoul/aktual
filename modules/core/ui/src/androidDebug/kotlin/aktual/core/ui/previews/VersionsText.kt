/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui.previews

import aktual.core.model.AktualVersions
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.VersionsText
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewServerNull() = PreviewThemedColumn {
  VersionsText(AktualVersions(app = "1.2.3", server = null))
}

@Preview
@Composable
private fun PreviewBothVersions() = PreviewThemedColumn {
  VersionsText(AktualVersions(app = "1.2.3", server = "2.3.4"))
}
