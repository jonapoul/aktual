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
package actual.preview

import actual.budget.model.WidgetId
import actual.budget.reports.ui.ReportsDashboardScaffold
import actual.budget.reports.ui.charts.PreviewCashFlow
import actual.budget.reports.ui.charts.PreviewNetWorth
import actual.budget.reports.ui.charts.PreviewSummary
import actual.budget.reports.vm.DashboardState
import actual.budget.reports.vm.ReportDashboardItem
import actual.core.ui.PreviewThemedScreen
import actual.core.ui.TripleScreenPreview
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
