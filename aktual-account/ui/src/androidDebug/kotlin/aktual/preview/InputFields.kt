/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.account.ui.url.InputFields
import aktual.core.model.Protocol
import aktual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun Empty() = PreviewThemedColumn {
  InputFields(
    url = "",
    protocol = Protocol.Http,
    onAction = {},
  )
}

@Preview
@Composable
private fun Filled() = PreviewThemedColumn {
  InputFields(
    url = "my.server.com:1234/path",
    protocol = Protocol.Https,
    onAction = {},
  )
}
