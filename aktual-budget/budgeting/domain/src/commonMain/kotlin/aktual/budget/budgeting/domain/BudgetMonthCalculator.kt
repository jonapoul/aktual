package aktual.budget.budgeting.domain

import aktual.budget.db.BudgetCategories
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth

interface BudgetMonthCalculator {
  fun observe(month: YearMonth): Flow<BudgetMonth>
}

@ContributesBinding(BudgetScope::class)
class BudgetMonthCalculatorImpl(
  private val budgetDao: BudgetDao,
  private val preferencesDao: PreferencesDao,
  private val calendar: Calendar,
  private val contexts: CoroutineContexts,
) : BudgetMonthCalculator {
  override fun observe(month: YearMonth): Flow<BudgetMonth> =
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
          Envelope -> observeEnvelope(start, month)
          Tracking -> observeTracking(start, month)
        }
      }
      .distinctUntilChanged()
      .flowOn(contexts.default)

  private fun observeEnvelope(start: YearMonth, month: YearMonth): Flow<BudgetMonth> =
    combine(
      budgetDao.observeCategories(),
      budgetDao.observeSpentByMonth(start.firstDay, month.lastDay),
      budgetDao.observeEnvelopeBudgets(start, month),
      budgetDao.observeEnvelopeMonths(),
    ) { categories, spent, budgets, months ->
      val buffered = months.mapNotNull { row ->
        row.id.value.toYearMonthOrNull()?.let { it to (row.buffered ?: Amount.Zero) }
      }
      budgetData(categories, spent, budgets, buffered.toMap()).envelopeMonth(start, month)
    }

  private fun observeTracking(start: YearMonth, month: YearMonth): Flow<BudgetMonth> =
    combine(
      budgetDao.observeCategories(),
      budgetDao.observeSpentByMonth(start.firstDay, month.lastDay),
      budgetDao.observeTrackingBudgets(start, month),
    ) { categories, spent, budgets ->
      budgetData(categories, spent, budgets).trackingMonth(start, month)
    }
}

private const val YEAR_MONTH_FACTOR = 100

private fun budgetData(
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
