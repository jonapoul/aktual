package aktual.budget.budgeting.domain

import aktual.budget.db.dao.CategoryBudget
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth

internal data class BudgetCategory(
  val id: CategoryId,
  val name: String,
  val group: CategoryGroupId,
  val isIncome: Boolean,
  val isHidden: Boolean,
  val isGroupIncome: Boolean,
  val isGroupHidden: Boolean,
)

internal data class BudgetGroup(
  val id: CategoryGroupId,
  val name: String,
  val isIncome: Boolean,
  val isHidden: Boolean,
  val sortOrder: Double?,
)

internal data class MonthCategory(val month: YearMonth, val category: CategoryId)

// Categories must be ordered by group, with the first income group's categories ahead of any other
// income group's
internal data class BudgetData(
  val categories: List<BudgetCategory>,
  val spent: Map<MonthCategory, Amount>,
  val budgets: Map<MonthCategory, CategoryBudget>,
  val buffered: Map<YearMonth, Amount> = emptyMap(),
  val groups: List<BudgetGroup> = emptyList(),
) {
  // Upstream only reads the first income group
  val incomeGroup: CategoryGroupId? = categories.firstOrNull { it.isGroupIncome }?.group
}

private const val LEAD_MONTHS = 3
private const val TRAILING_MONTHS = 12

// packages/loot-core/src/server/budget/base.ts getBudgetRange(). Budgets start three months before
// the earliest transaction, or the current month if that comes first
internal fun budgetStart(earliestTransaction: LocalDate?, today: LocalDate): YearMonth {
  val current = today.yearMonth
  val earliest = earliestTransaction?.yearMonth ?: current
  return minOf(earliest, current).minus(LEAD_MONTHS, MONTH)
}

// packages/loot-core/src/server/budget/base.ts getBudgetRange(). Budgets end a year after the
// current month, future-dated transactions don't extend them
internal fun budgetBounds(
  earliestTransaction: LocalDate?,
  today: LocalDate,
): ClosedRange<YearMonth> =
  budgetStart(earliestTransaction, today)..today.yearMonth.plus(TRAILING_MONTHS, MONTH)

// Walks forward from the first budget month once, keeping every month in the range
private inline fun <T : BudgetMonth> walk(
  start: YearMonth,
  months: ClosedRange<YearMonth>,
  blank: (YearMonth) -> T,
  next: (month: YearMonth, previous: T) -> T,
): List<T> {
  val first = minOf(start, months.start)
  var result = blank(first)
  return buildList {
    for (current in first..months.endInclusive) {
      result = next(current, result)
      if (current in months) add(result)
    }
  }
}

// packages/loot-core/src/server/budget/envelope.ts. Each month depends on the one before, so walk
// forward from the first budget month
internal fun BudgetData.envelopeMonths(
  start: YearMonth,
  months: ClosedRange<YearMonth>,
): List<BudgetMonth.Envelope> =
  walk(start, months, ::blankEnvelope) { month, previous ->
    envelopeMonth(month, previous)
  }

internal fun BudgetData.envelopeMonth(start: YearMonth, month: YearMonth): BudgetMonth.Envelope =
  envelopeMonths(start, month..month).single()

private fun blankEnvelope(month: YearMonth) =
  BudgetMonth.Envelope(
    month = month,
    toBudget = Zero,
    budgeted = Zero,
    spent = Zero,
    balance = Zero,
    income = Zero,
    fromLastMonth = Zero,
    lastMonthOverspent = Zero,
    buffered = Zero,
    categories = persistentListOf(),
  )

private fun BudgetData.envelopeMonth(
  month: YearMonth,
  previous: BudgetMonth.Envelope,
): BudgetMonth.Envelope {
  val previousById = previous.categories.associateBy { it.id }
  val rows = categories.map { category ->
    val key = MonthCategory(month, category.id)
    val budgeted = budgets[key]?.amount ?: Amount.Zero
    val spent = spent[key] ?: Amount.Zero
    val carried =
      previousById[category.id]?.let {
        if (it.carryover) it.balance else maxOf(it.balance, Amount.Zero)
      }
    category.toMonth(
      budgeted = budgeted,
      spent = spent,
      // Income categories have no balance, what they receive goes to the month's funds
      balance = if (category.isIncome) Zero else budgeted + spent + (carried ?: Amount.Zero),
      carryover = budgets[key]?.carryover == true,
    )
  }

  // Unlike tracking budgets, upstream counts hidden categories and groups here
  val expenses = rows.filterBy(categories) { !it.isGroupIncome }
  val income = rows.filterBy(categories) { it.group == incomeGroup }.sumOf { it.spent }
  val fromLastMonth = previous.toBudget + previous.buffered
  val lastMonthOverspent =
    rows
      .asSequence()
      .filter { !it.isIncome }
      .mapNotNull { previousById[it.id] }
      .filter { !it.carryover }
      .toList()
      .sumOf { minOf(it.balance, Amount.Zero) }
  val budgeted = expenses.sumOf { it.budgeted }

  // Holding an income category for next month buffers what it received, unless an amount was set
  val manualBuffer = buffered[month] ?: Amount.Zero
  val buffer =
    if (manualBuffer != Zero) {
      manualBuffer
    } else {
      rows.filter { it.isIncome && it.carryover }.sumOf { it.spent }
    }

  return BudgetMonth.Envelope(
    month = month,
    toBudget = income + fromLastMonth + lastMonthOverspent - budgeted - buffer,
    budgeted = budgeted,
    spent = expenses.sumOf { it.spent },
    balance = expenses.sumOf { it.balance },
    income = income,
    fromLastMonth = fromLastMonth,
    lastMonthOverspent = lastMonthOverspent,
    buffered = buffer,
    categories = rows.toImmutableList(),
    // Hidden categories count here too, and income groups have no budget or leftover
    groups =
      groupMonths(rows, counts = { true }) { group, totals ->
        if (group.isIncome) totals.copy(budgeted = Zero, balance = Zero) else totals
      },
  )
}

// packages/loot-core/src/server/budget/tracking.ts. Only a carried-over balance links one month to
// the next
internal fun BudgetData.trackingMonths(
  start: YearMonth,
  months: ClosedRange<YearMonth>,
): List<BudgetMonth.Tracking> =
  walk(start, months, ::blankTracking) { month, previous ->
    trackingMonth(month, previous)
  }

internal fun BudgetData.trackingMonth(start: YearMonth, month: YearMonth): BudgetMonth.Tracking =
  trackingMonths(start, month..month).single()

private fun blankTracking(month: YearMonth) =
  BudgetMonth.Tracking(
    month = month,
    budgeted = Zero,
    spent = Zero,
    balance = Zero,
    income = Zero,
    incomeBudgeted = Zero,
    categories = persistentListOf(),
  )

private fun BudgetData.trackingMonth(
  month: YearMonth,
  previous: BudgetMonth.Tracking,
): BudgetMonth.Tracking {
  val previousById = previous.categories.associateBy { it.id }
  val rows = categories.map { category ->
    val key = MonthCategory(month, category.id)
    val budgeted = budgets[key]?.amount ?: Amount.Zero
    val spent = spent[key] ?: Amount.Zero
    val carried = previousById[category.id]?.takeIf { it.carryover }?.balance ?: Amount.Zero
    category.toMonth(
      budgeted = budgeted,
      spent = spent,
      // For income this is what's still to be received
      balance = budgeted + (if (category.isIncome) -spent else spent) + carried,
      carryover = budgets[key]?.carryover == true,
    )
  }

  // Hidden categories and groups are left out of the totals
  val expenses =
    rows.filterBy(categories) { !it.isHidden && !it.isGroupIncome && !it.isGroupHidden }
  val income = rows.filterBy(categories) { !it.isHidden && it.group == incomeGroup }

  return BudgetMonth.Tracking(
    month = month,
    budgeted = expenses.sumOf { it.budgeted },
    spent = expenses.sumOf { it.spent },
    balance = expenses.sumOf { it.balance },
    income = income.sumOf { it.spent },
    incomeBudgeted = income.sumOf { it.budgeted },
    categories = rows.toImmutableList(),
    groups = groupMonths(rows, counts = { !it.isHidden }) { _, totals -> totals },
  )
}

private fun BudgetCategory.toMonth(
  budgeted: Amount,
  spent: Amount,
  balance: Amount,
  carryover: Boolean,
) =
  CategoryMonth(
    id = id,
    name = name,
    group = group,
    isIncome = isIncome,
    isHidden = isHidden,
    budgeted = budgeted,
    spent = spent,
    balance = balance,
    carryover = carryover,
  )

// packages/loot-core/src/server/budget/{envelope,tracking}.ts createCategoryGroup()
private inline fun BudgetData.groupMonths(
  rows: List<CategoryMonth>,
  counts: (BudgetCategory) -> Boolean,
  adjust: (BudgetGroup, CategoryGroupMonth) -> CategoryGroupMonth,
): ImmutableList<CategoryGroupMonth> {
  val byGroup = rows.indices.groupBy { categories[it].group }
  return groups
    .map { group ->
      val indices = byGroup[group.id].orEmpty()
      val counted = indices.filter { counts(categories[it]) }.map { rows[it] }
      val totals =
        CategoryGroupMonth(
          id = group.id,
          name = group.name,
          isIncome = group.isIncome,
          isHidden = group.isHidden,
          sortOrder = group.sortOrder,
          budgeted = counted.sumOf { it.budgeted },
          spent = counted.sumOf { it.spent },
          balance = counted.sumOf { it.balance },
          categories = indices.map { rows[it] }.toImmutableList(),
        )
      adjust(group, totals)
    }
    .toImmutableList()
}

// Rows line up with the categories they were built from
private inline fun List<CategoryMonth>.filterBy(
  categories: List<BudgetCategory>,
  predicate: (BudgetCategory) -> Boolean,
): List<CategoryMonth> = filterIndexed { index, _ -> predicate(categories[index]) }

private inline fun <T> Iterable<T>.sumOf(selector: (T) -> Amount): Amount =
  fold(Amount.Zero) { sum, item -> sum + selector(item) }
