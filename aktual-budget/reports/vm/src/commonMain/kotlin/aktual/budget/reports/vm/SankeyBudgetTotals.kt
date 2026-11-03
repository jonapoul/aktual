package aktual.budget.reports.vm

import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.rules.domain.FilterRow
import aktual.budget.rules.domain.TransactionFilter

// packages/desktop-client/src/components/reports/spreadsheets/sankey-spreadsheet.ts
// createBudgetSpreadsheet(), over the range's months and the month after it. Tracking budgets have
// no to budget or carried over amounts, so those stay at zero like upstream's empty cells
internal fun sankeyBudget(
  months: List<BudgetMonth>,
  next: BudgetMonth?,
  conditions: List<Condition>?,
  op: ConditionOp?,
): SankeyBudget {
  val filter = categoryFilter(conditions.orEmpty(), op ?: And)
  val totals = LinkedHashMap<String, SankeyBudgetEntry>()
  for (month in months) {
    for (group in month.groups) {
      for (category in group.categories) {
        val value = if (group.isIncome) category.spent else category.budgeted
        val key = category.id.value
        val existing = totals[key]
        totals[key] =
          existing?.copy(value = existing.value + value.toLong())
            ?: SankeyBudgetEntry(
              categoryGroupId = group.id.value,
              categoryGroup = group.name,
              categoryId = key,
              category = category.name,
              isIncome = group.isIncome,
              value = value.toLong(),
            )
      }
    }
  }

  val first = months.firstOrNull() as? Envelope
  val last = months.lastOrNull() as? Envelope
  val toBudget = last?.toBudget?.toLong() ?: 0L
  val nextFromLastMonth = (next as? Envelope)?.fromLastMonth?.toLong() ?: 0L
  return SankeyBudget(
    entries = totals.values.filter { it.value != 0L && filter.matches(it) },
    toBudget = toBudget,
    fromPreviousMonth = first?.fromLastMonth?.toLong() ?: 0L,
    lastMonthOverspent =
      months.filterIsInstance<BudgetMonth.Envelope>().sumOf { it.lastMonthOverspent.toLong() },
    forNextMonth = nextFromLastMonth - toBudget,
  )
}

// Groups then their categories, in budget order
internal fun budgetCategoryOrder(month: BudgetMonth?): List<String> =
  month?.groups.orEmpty().flatMap { group ->
    [group.id.value] + group.categories.map { it.id.value }
  }

// Budget amounts aren't transactions, so only the category and category group conditions apply,
// like upstream's filterCategoryGroups()
private fun categoryFilter(conditions: List<Condition>, op: ConditionOp): TransactionFilter =
  TransactionFilter.compile(
    conditions.filter {
      it.customName == null && (it.field == Category || it.field == CategoryGroup)
    },
    op,
  )

private fun TransactionFilter.matches(entry: SankeyBudgetEntry): Boolean =
  isEmpty ||
    matches(
      FilterRow(
        date = ALL_TIME_START,
        amount = Zero,
        account = AccountId(""),
        category = CategoryId(entry.categoryId),
        categoryName = entry.category,
        categoryGroup = CategoryGroupId(entry.categoryGroupId),
        categoryGroupName = entry.categoryGroup,
      ),
    )
