/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.list.ui.BudgetStateText
import aktual.budget.model.BudgetState
import aktual.core.ui.PreviewThemedRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewStates() = PreviewThemedRow {
  LazyColumn {
    items(BudgetState.entries) {
      BudgetStateText(it)
    }
  }
}
