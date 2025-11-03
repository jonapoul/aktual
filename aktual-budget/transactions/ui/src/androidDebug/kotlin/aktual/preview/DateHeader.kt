/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.transactions.ui.DateHeader
import aktual.budget.transactions.ui.StateSource
import aktual.core.ui.PreviewThemedColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewDateHeader() = PreviewThemedColumn {
  DateHeader(
    date = PREVIEW_DATE,
    source = StateSource.Empty,
    onAction = {},
  )
}
