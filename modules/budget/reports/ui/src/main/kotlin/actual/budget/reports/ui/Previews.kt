package actual.budget.reports.ui

import actual.budget.model.Amount
import actual.budget.model.CustomReportId
import actual.budget.model.DateRangeType
import actual.budget.model.ReportDate
import actual.budget.model.YearAndMonth
import actual.budget.reports.vm.ReportDashboardItem
import actual.budget.reports.vm.ReportRange
import actual.budget.reports.vm.ReportValues
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.Month.OCTOBER

internal val ITEM_1 = ReportDashboardItem(
  id = CustomReportId("abc-123"),
  name = "Pensions",
  range = ReportRange.Dynamic(DateRangeType.Last12Months),
  values = ReportValues.None,
)

internal val ITEM_2 = ReportDashboardItem(
  id = CustomReportId("def-456"),
  name = "Groceries",
  range = ReportRange.Static(
    start = ReportDate.Month(YearAndMonth(2018, OCTOBER)),
    end = ReportDate.Month(YearAndMonth(2025, JUNE)),
  ),
  values = ReportValues.None,
)

internal val ITEM_3 = ReportDashboardItem(
  id = CustomReportId("xyz-789"),
  name = "Pensions",
  range = ReportRange.Dynamic(DateRangeType.AllTime),
  values = ReportValues.Shown(amount = Amount(12345.67), change = Amount(789.01)),
)
