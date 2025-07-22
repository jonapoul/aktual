package actual.budget.reports.ui

import actual.budget.model.WidgetId
import actual.budget.reports.ui.charts.PreviewCashFlow
import actual.budget.reports.ui.charts.PreviewNetWorth
import actual.budget.reports.ui.charts.PreviewSummary
import actual.budget.reports.vm.ReportDashboardItem
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

internal fun date(year: Int, month: Month) = YearMonth(year, month)

internal object ReportDashboardItems {
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
}
