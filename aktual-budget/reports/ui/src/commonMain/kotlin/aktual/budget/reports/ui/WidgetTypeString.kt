package aktual.budget.reports.ui

import aktual.budget.model.WidgetType
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable

@Composable
internal fun WidgetType.string() =
  when (this) {
    WidgetType.NetWorth -> Strings.reportsChooseTypeNetWorth
    WidgetType.CashFlow -> Strings.reportsChooseTypeCashFlow
    WidgetType.Spending -> Strings.reportsChooseTypeSpending
    WidgetType.Custom -> Strings.reportsChooseTypeCustom
    WidgetType.Markdown -> Strings.reportsChooseTypeMarkdown
    WidgetType.Summary -> Strings.reportsChooseTypeSummary
    WidgetType.Calendar -> Strings.reportsChooseTypeCalendar
    WidgetType.BudgetAnalysis -> Strings.reportsChooseTypeBudgetAnalysis
  }
