/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.list.ui.ContentSuccess
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.persistentListOf

@TripleScreenPreview
@Composable
private fun Three() = PreviewThemedScreen {
  ContentSuccess(
    modifier = Modifier.background(LocalTheme.current.pageBackground),
    budgets = persistentListOf(PreviewBudgetSynced, PreviewBudgetSynced, PreviewBudgetSynced),
    onClickOpen = {},
    onClickDelete = {},
  )
}
