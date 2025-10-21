/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.sync.ui.Content
import aktual.core.model.Password
import aktual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewEmpty() = PreviewThemedColumn {
  Content(
    input = Password.Empty,
    onAction = {},
  )
}

@Preview
@Composable
private fun PreviewFull() = PreviewThemedColumn {
  Content(
    input = Password("abc-123"),
    onAction = {},
  )
}
