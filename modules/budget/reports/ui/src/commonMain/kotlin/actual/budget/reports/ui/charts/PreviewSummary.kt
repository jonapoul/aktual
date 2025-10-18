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
package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.reports.vm.PercentageDivisor
import actual.budget.reports.vm.SummaryData
import actual.core.model.percent

internal object PreviewSummary {
  val SUM_DATA = SummaryData.Sum(
    title = "My Sum Summary",
    start = PreviewShared.START_DATE,
    end = PreviewShared.END_DATE,
    value = PreviewShared.AMOUNT,
  )

  val PER_MONTH_DATA = SummaryData.AveragePerMonth(
    title = "My Per-Month Summary",
    start = PreviewShared.START_DATE,
    end = PreviewShared.END_DATE,
    numMonths = 18.19f,
    total = Amount(6198.55),
    average = Amount(340.70),
  )

  val PER_TRANSACTION_DATA = SummaryData.AveragePerTransaction(
    title = "My Per-Transaction Summary",
    start = PreviewShared.START_DATE,
    end = PreviewShared.END_DATE,
    numTransactions = 1327,
    total = Amount(6198.55),
    average = Amount(340.70),
  )

  val PERCENT_DATA = SummaryData.Percentage(
    title = "My Percent Summary",
    start = PreviewShared.START_DATE,
    end = PreviewShared.END_DATE,
    numerator = Amount(6198.55),
    denominator = Amount(4043.87),
    percent = 153.28.percent,
    divisor = PercentageDivisor.AllTime,
  )
}
