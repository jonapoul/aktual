/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.preview

import aktual.budget.model.WidgetId
import aktual.budget.reports.ui.ReportsDashboardScaffold
import aktual.budget.reports.ui.charts.PreviewCashFlow
import aktual.budget.reports.ui.charts.PreviewNetWorth
import aktual.budget.reports.ui.charts.PreviewSummary
import aktual.budget.reports.vm.DashboardState
import aktual.budget.reports.vm.ReportDashboardItem
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf

@TripleScreenPreview
@Composable
private fun PreviewLoaded() = PreviewThemedScreen {
  ReportsDashboardScaffold(
    state = DashboardState.Loaded(
      items = persistentListOf(ITEM_1, ITEM_2, ITEM_3),
    ),
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun PreviewLoading() = PreviewThemedScreen {
  ReportsDashboardScaffold(
    state = DashboardState.Loading,
    onAction = {},
  )
}

@TripleScreenPreview
@Composable
private fun PreviewEmpty() = PreviewThemedScreen {
  ReportsDashboardScaffold(
    state = DashboardState.Empty,
    onAction = {},
  )
}

internal val ITEM_1 = ReportDashboardItem(
  id = WidgetId("abc-123"),
  name = "Pensions",
  data = PreviewCashFlow.DATA,
)

internal val ITEM_2 = ReportDashboardItem(
  id = WidgetId("def-456"),
  name = "Groceries",
  data = PreviewNetWorth.DATA,
)

internal val ITEM_3 = ReportDashboardItem(
  id = WidgetId("xyz-789"),
  name = "Pensions",
  data = PreviewSummary.PER_TRANSACTION_DATA,
)
