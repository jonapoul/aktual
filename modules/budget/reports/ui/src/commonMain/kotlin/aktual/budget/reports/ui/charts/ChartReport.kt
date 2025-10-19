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
package aktual.budget.reports.ui.charts

import aktual.budget.reports.ui.ActionListener
import aktual.budget.reports.vm.CalendarData
import aktual.budget.reports.vm.CashFlowData
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.NetWorthData
import aktual.budget.reports.vm.SpendingData
import aktual.budget.reports.vm.SummaryData
import aktual.budget.reports.vm.TextData
import aktual.core.ui.Theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ReportChart(
  data: ChartData,
  compact: Boolean,
  theme: Theme,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  includeHeader: Boolean = true,
) = when (data) {
  is CashFlowData -> CashFlowChart(data, compact, modifier, theme, includeHeader)
  is NetWorthData -> NetWorthChart(data, compact, modifier, theme, includeHeader)
  is SummaryData -> SummaryChart(data, compact, onAction, modifier, theme, includeHeader)
  is CalendarData -> CalendarChart(data, compact, onAction, modifier, theme, includeHeader)
  is SpendingData -> SpendingChart(data, compact, modifier, theme, includeHeader)
  is TextData -> TextChart(data, compact, onAction, modifier, theme)
  is CustomData -> CustomChart(data, compact, modifier, theme, includeHeader)
}
