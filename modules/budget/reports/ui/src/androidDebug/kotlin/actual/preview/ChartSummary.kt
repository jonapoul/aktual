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

import actual.budget.reports.ui.charts.PreviewShared.END_DATE
import actual.budget.reports.ui.charts.PreviewShared.START_DATE
import actual.budget.reports.ui.charts.PreviewShared.WIDTH
import actual.budget.reports.ui.charts.PreviewSummary
import actual.budget.reports.ui.charts.SummaryChart
import actual.budget.reports.vm.PercentageDivisor
import actual.budget.reports.vm.SummaryData
import actual.core.ui.CardShape
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewThemedColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(widthDp = WIDTH)
@Composable
private fun SumCompact() = PreviewChart(PreviewSummary.SUM_DATA, true)

@Preview(widthDp = WIDTH)
@Composable
private fun PercentCompact() = PreviewChart(PreviewSummary.PERCENT_DATA, true)

@Preview(widthDp = WIDTH)
@Composable
private fun SumRegular() = PreviewChart(PreviewSummary.SUM_DATA, false)

@Preview(widthDp = WIDTH)
@Composable
private fun SumRegularPrivate() = PreviewChart(PreviewSummary.SUM_DATA, false, private = true)

@Preview(widthDp = WIDTH)
@Composable
private fun PerMonthRegular() = PreviewChart(PreviewSummary.PER_MONTH_DATA, false)

@Preview(widthDp = WIDTH)
@Composable
private fun PerTransactionRegular() = PreviewChart(PreviewSummary.PER_TRANSACTION_DATA, false)

@Preview(widthDp = WIDTH)
@Composable
private fun PerTransactionPrivate() = PreviewChart(PreviewSummary.PER_TRANSACTION_DATA, false, private = true)

@Preview(widthDp = WIDTH)
@Composable
private fun PercentAllTime() = PreviewChart(
  data = PreviewSummary.PERCENT_DATA.copy(divisor = PercentageDivisor.AllTime),
  compact = false,
)

@Preview(widthDp = WIDTH)
@Composable
private fun PercentSpecific() = PreviewChart(
  data = PreviewSummary.PERCENT_DATA.copy(divisor = PercentageDivisor.Specific(START_DATE, END_DATE)),
  compact = false,
)

@Composable
private fun PreviewChart(
  data: SummaryData,
  compact: Boolean,
  private: Boolean = false,
) = PreviewThemedColumn(isPrivacyEnabled = private) {
  SummaryChart(
    modifier = Modifier
      .background(LocalTheme.current.tableBackground, CardShape)
      .width(WIDTH.dp)
      .padding(5.dp),
    compact = compact,
    data = data,
    onAction = {},
  )
}
