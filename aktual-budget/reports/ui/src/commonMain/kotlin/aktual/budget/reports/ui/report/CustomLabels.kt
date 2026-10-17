package aktual.budget.reports.ui.report

import aktual.budget.model.BalanceType
import aktual.budget.model.CustomReportMode
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.SortBy
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable

@Composable
internal fun CustomReportMode.label(): String =
  when (this) {
    Total -> Strings.reportsCustomEditorModeTotal
    Time -> Strings.reportsCustomEditorModeTime
    Unknown -> Strings.reportsCustomEditorUnknown
  }

@Composable
internal fun GraphType.label(): String =
  when (this) {
    TableGraph -> Strings.reportsCustomEditorGraphTable
    BarGraph -> Strings.reportsCustomEditorGraphBar
    StackedBarGraph -> Strings.reportsCustomEditorGraphStackedBar
    LineGraph -> Strings.reportsCustomEditorGraphLine
    AreaGraph -> Strings.reportsCustomEditorGraphArea
    DonutGraph -> Strings.reportsCustomEditorGraphDonut
    BarLineGraph -> Strings.reportsCustomEditorGraphBarLine
    Unknown -> Strings.reportsCustomEditorUnknown
  }

@Composable
internal fun GroupBy.label(): String =
  when (this) {
    Category -> Strings.reportsCustomEditorSplitCategory
    Group -> Strings.reportsCustomEditorSplitGroup
    CategoryGroup -> Strings.reportsCustomEditorSplitCategoryGroup
    Payee -> Strings.reportsCustomEditorSplitPayee
    Account -> Strings.reportsCustomEditorSplitAccount
    GroupBy.Interval -> Strings.reportsCustomEditorSplitInterval
    Unknown -> Strings.reportsCustomEditorUnknown
  }

@Composable
internal fun BalanceType.label(): String =
  when (this) {
    Payment -> Strings.reportsCustomEditorTypePayment
    Deposit -> Strings.reportsCustomEditorTypeDeposit
    Net -> Strings.reportsCustomEditorTypeNet
    NetPayment -> Strings.reportsCustomEditorTypeNetPayment
    NetDeposit -> Strings.reportsCustomEditorTypeNetDeposit
    Budgeted -> Strings.reportsCustomEditorTypeBudgeted
    Expense -> Strings.reportsCustomEditorTypeExpense
    Unknown -> Strings.reportsCustomEditorUnknown
  }

@Composable
internal fun Interval.label(): String =
  when (this) {
    Daily -> Strings.reportsCustomEditorIntervalDaily
    Weekly -> Strings.reportsCustomEditorIntervalWeekly
    Monthly -> Strings.reportsCustomEditorIntervalMonthly
    Yearly -> Strings.reportsCustomEditorIntervalYearly
    Unknown -> Strings.reportsCustomEditorUnknown
  }

@Composable
internal fun SortBy.label(): String =
  when (this) {
    Asc -> Strings.reportsCustomEditorSortAsc
    Desc -> Strings.reportsCustomEditorSortDesc
    Name -> Strings.reportsCustomEditorSortName
    Budget -> Strings.reportsCustomEditorSortBudget
    Unknown -> Strings.reportsCustomEditorUnknown
  }
