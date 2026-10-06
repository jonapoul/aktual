package aktual.budget.reports.ui

import aktual.budget.model.WidgetType
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable

@Composable
internal fun WidgetType.string() =
  when (this) {
    NetWorth -> Strings.reportsChooseTypeNetWorth
    CashFlow -> Strings.reportsChooseTypeCashFlow
    Spending -> Strings.reportsChooseTypeSpending
    Custom -> Strings.reportsChooseTypeCustom
    Markdown -> Strings.reportsChooseTypeMarkdown
    Summary -> Strings.reportsChooseTypeSummary
    Calendar -> Strings.reportsChooseTypeCalendar
    BudgetAnalysis -> Strings.reportsChooseTypeBudgetAnalysis
    Formula -> Strings.reportsChooseTypeFormula
    Crossover -> Strings.reportsChooseTypeCrossover
    Sankey -> Strings.reportsChooseTypeSankey
    BalanceForecast -> Strings.reportsChooseTypeBalanceForecast
    AgeOfMoney -> Strings.reportsChooseTypeAgeOfMoney
    MonteCarlo -> Strings.reportsChooseTypeMonteCarlo
    Unknown -> Strings.reportsChooseTypeUnknown
  }
