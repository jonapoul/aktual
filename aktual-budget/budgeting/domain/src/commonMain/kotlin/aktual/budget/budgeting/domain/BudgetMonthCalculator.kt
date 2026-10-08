package aktual.budget.budgeting.domain

import aktual.budget.db.BudgetCategories
import aktual.budget.db.BudgetCategoryGroups
import aktual.budget.db.BudgetSpentByMonth
import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.CategoryBudget
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.SyncedPrefKey
import aktual.core.Calendar
import aktual.di.BudgetScope
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.ContributesBinding
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth

interface BudgetMonthCalculator {
  fun observe(month: YearMonth): Flow<BudgetMonth>

  // One walk from the budget start covers every month in the range
  fun observeRange(months: ClosedRange<YearMonth>): Flow<ImmutableList<BudgetMonth>>

  // packages/loot-core/src/server/budget/app.ts get-budget-bounds
  fun observeBounds(): Flow<ClosedRange<YearMonth>>
}

@ContributesBinding(BudgetScope::class)
class BudgetMonthCalculatorImpl(
  private val budgetDao: BudgetDao,
  private val preferencesDao: PreferencesDao,
  private val calendar: Calendar,
  private val contexts: CoroutineContexts,
) : BudgetMonthCalculator {
  override fun observe(month: YearMonth): Flow<BudgetMonth> =
    observeRange(month..month).map { it.single() }.distinctUntilChanged()

  override fun observeRange(months: ClosedRange<YearMonth>): Flow<ImmutableList<BudgetMonth>> =
    combine(
        preferencesDao.observe(SyncedPrefKey.Global.BudgetType).map { BudgetType.from(it) },
        budgetDao.observeEarliestTransactionDate(),
      ) { type, earliest ->
        // Upstream falls back to envelope budgeting too
        (type ?: Envelope) to budgetStart(earliest, calendar.today())
      }
      .distinctUntilChanged()
      .flatMapLatest { (type, start) ->
        when (type) {
          Envelope -> observeEnvelope(start, months)
          Tracking -> observeTracking(start, months)
        }
      }
      .distinctUntilChanged()
      .flowOn(contexts.default)

  override fun observeBounds(): Flow<ClosedRange<YearMonth>> =
    budgetDao
      .observeEarliestTransactionDate()
      .map { earliest -> budgetBounds(earliest, calendar.today()) }
      .distinctUntilChanged()
      .flowOn(contexts.default)

  private fun observeEnvelope(
    start: YearMonth,
    months: ClosedRange<YearMonth>,
  ): Flow<ImmutableList<BudgetMonth>> {
    val end = months.endInclusive
    return combine(
      budgetDao.observeCategoryGroups(),
      budgetDao.observeCategories(),
      budgetDao.observeSpentByMonth(start.firstDay, end.lastDay),
      budgetDao.observeEnvelopeBudgets(start, end),
      budgetDao.observeEnvelopeMonths(),
    ) { groups, categories, spent, budgets, budgetMonths ->
      val buffered = budgetMonths.mapNotNull { row ->
        row.id.value.toYearMonthOrNull()?.let { it to (row.buffered ?: Amount.Zero) }
      }
      budgetData(groups, categories, spent, budgets, buffered.toMap())
        .envelopeMonths(start, months)
        .toImmutableList()
    }
  }

  private fun observeTracking(
    start: YearMonth,
    months: ClosedRange<YearMonth>,
  ): Flow<ImmutableList<BudgetMonth>> {
    val end = months.endInclusive
    return combine(
      budgetDao.observeCategoryGroups(),
      budgetDao.observeCategories(),
      budgetDao.observeSpentByMonth(start.firstDay, end.lastDay),
      budgetDao.observeTrackingBudgets(start, end),
    ) { groups, categories, spent, budgets ->
      budgetData(groups, categories, spent, budgets).trackingMonths(start, months).toImmutableList()
    }
  }
}

private const val YEAR_MONTH_FACTOR = 100

private fun budgetData(
  groups: List<BudgetCategoryGroups>,
  categories: List<BudgetCategories>,
  spent: List<BudgetSpentByMonth>,
  budgets: List<CategoryBudget>,
  buffered: Map<YearMonth, Amount> = emptyMap(),
) =
  BudgetData(
    categories =
      categories.map { row ->
        BudgetCategory(
          id = row.id,
          name = row.name.orEmpty(),
          group = row.group_id,
          isIncome = row.is_income == true,
          isHidden = row.hidden,
          isGroupIncome = row.group_is_income == true,
          isGroupHidden = row.group_hidden,
        )
      },
    spent =
      spent.associate { row ->
        MonthCategory(row.month.toYearMonth(), row.category) to Amount(row.total)
      },
    budgets = budgets.associateBy { MonthCategory(it.month, it.category) },
    buffered = buffered,
    groups =
      groups.map { row ->
        BudgetGroup(
          id = row.id,
          name = row.name.orEmpty(),
          isIncome = row.is_income == true,
          isHidden = row.hidden,
          sortOrder = row.sort_order,
        )
      },
  )

// Stored as YYYYMM
private fun Long.toYearMonth(): YearMonth =
  YearMonth(year = (this / YEAR_MONTH_FACTOR).toInt(), month = (this % YEAR_MONTH_FACTOR).toInt())

// Budget month IDs look like "2026-03"
private fun String.toYearMonthOrNull(): YearMonth? =
  try {
    YearMonth.parse(this)
  } catch (_: IllegalArgumentException) {
    null
  }
