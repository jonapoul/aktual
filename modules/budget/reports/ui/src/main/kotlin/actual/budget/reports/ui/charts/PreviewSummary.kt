package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.reports.vm.PercentageDivisor
import actual.budget.reports.vm.SummaryData
import actual.core.model.percent

internal object PreviewSummary {
  val SUM_DATA = SummaryData.Sum(
    start = PreviewShared.START_DATE,
    end = PreviewShared.END_DATE,
    value = PreviewShared.AMOUNT,
  )

  val PER_MONTH_DATA = SummaryData.AveragePerMonth(
    start = PreviewShared.START_DATE,
    end = PreviewShared.END_DATE,
    numMonths = 18.19f,
    total = Amount(6198.55),
    average = Amount(340.70),
  )

  val PER_TRANSACTION_DATA = SummaryData.AveragePerTransaction(
    start = PreviewShared.START_DATE,
    end = PreviewShared.END_DATE,
    numTransactions = 1327,
    total = Amount(6198.55),
    average = Amount(340.70),
  )

  val PERCENT_DATA = SummaryData.Percentage(
    start = PreviewShared.START_DATE,
    end = PreviewShared.END_DATE,
    numerator = Amount(6198.55),
    denominator = Amount(4043.87),
    percent = 153.28.percent,
    divisor = PercentageDivisor.AllTime,
  )
}
