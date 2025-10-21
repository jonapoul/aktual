/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui.previews

import aktual.core.model.ServerUrl
import aktual.core.ui.PreviewThemedColumn
import aktual.core.ui.UsingServerText
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun DemoServer() = PreviewThemedColumn {
  UsingServerText(
    url = ServerUrl.Demo,
    onClickChange = {},
  )
}

@Preview
@Composable
private fun NoServer() = PreviewThemedColumn {
  UsingServerText(
    url = null,
    onClickChange = {},
  )
}
