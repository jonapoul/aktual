package aktual.budget.reports.vm

import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.budgeting.domain.CategoryMonth
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.YearMonth

// packages/desktop-client/src/components/reports/spreadsheets/budget-analysis-spreadsheet.ts
// createBudgetAnalysisSpreadsheet(). [months] starts with the month before the report, which only
// seeds the running balance
internal fun calculateBudgetAnalysis(
  months: List<BudgetMonth>,
  params: BudgetAnalysisParams,
): BudgetAnalysisData {
  val filter = BudgetCategoryFilter(params.conditions, params.conditionsOp)
  val groupNames = months.firstOrNull()?.groups?.associate { it.id to it.name }.orEmpty()
  val include = { category: CategoryMonth ->
    !category.isIncome &&
      (params.showHiddenCategories || !category.isHidden) &&
      filter.matches(
        category = category.id,
        categoryName = category.name,
        group = category.group,
        groupName = groupNames[category.group],
      )
  }

  val previous = months.firstOrNull()
  var runningBalance = previous?.totals(include)?.carryover ?: Zero
  var overspendingFromPrevious = Zero
  val items = LinkedHashMap<YearMonth, BudgetAnalysisDatum>()

  for (month in months.drop(1)) {
    val totals = month.totals(include)
    items[month.month] =
      BudgetAnalysisDatum(
        budgeted = totals.budgeted,
        spent = totals.spent,
        overspendingAdjustment = -overspendingFromPrevious,
        balance = totals.budgeted + totals.spent + runningBalance,
      )
    // Months that were never budgeted have nothing to carry over, so the balance passes through
    if (totals.hasBudgetData) runningBalance = totals.carryover
    overspendingFromPrevious = totals.overspending
  }

  return BudgetAnalysisData(
    title = params.title,
    items = items.toImmutableMap(),
    graphType = params.graphType,
    showBalance = params.showBalance,
    balanceOnly = params.balanceOnly,
  )
}

internal data class BudgetAnalysisParams(
  val title: String?,
  val conditions: List<Condition>,
  val conditionsOp: ConditionOp,
  val showHiddenCategories: Boolean,
  val graphType: GraphType,
  val showBalance: Boolean,
  val balanceOnly: Boolean,
)

// summarizeMonthCategories(). On envelope budgets positive balances always carry over, negative
// ones only with rollover overspending on. Otherwise they're reset, and show as next month's
// overspending adjustment. On tracking budgets any balance only carries over with carryover on,
// otherwise it's reset with no adjustment. Follows upstream's unmerged actualbudget/actual#8751
private data class MonthTotals(
  val budgeted: Amount,
  val spent: Amount,
  val carryover: Amount,
  val overspending: Amount,
  val hasBudgetData: Boolean,
)

private inline fun BudgetMonth.totals(include: (CategoryMonth) -> Boolean): MonthTotals {
  var budgeted = Zero
  var spent = Zero
  var carryover = Zero
  var overspending = Zero
  var hasBudgetData = false
  for (category in categories) {
    if (!include(category)) continue
    if (category.budgeted != Zero || category.spent != Zero || category.balance != Zero) {
      hasBudgetData = true
    }
    budgeted += category.budgeted
    spent += category.spent
    if (category.carryover || (this is Envelope && category.balance > Zero)) {
      carryover += category.balance
    } else if (this is Envelope) {
      overspending += category.balance
    }
  }
  return MonthTotals(budgeted, spent, carryover, overspending, hasBudgetData)
}
