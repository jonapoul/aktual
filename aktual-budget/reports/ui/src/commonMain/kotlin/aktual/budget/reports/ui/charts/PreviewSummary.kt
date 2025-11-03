/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.PercentageDivisor
import aktual.budget.reports.vm.SummaryData
import aktual.core.model.percent

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
