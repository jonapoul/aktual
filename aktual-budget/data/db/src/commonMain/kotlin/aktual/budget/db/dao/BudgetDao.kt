package aktual.budget.db.dao

import aktual.budget.db.BudgetCategories
import aktual.budget.db.BudgetCategoryGroups
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.BudgetSpentByMonth
import aktual.budget.db.Zero_budget_months
import aktual.budget.db.withResult
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

// A budget row of one month, with the flags of the category it's for
data class MonthBudget(
  val category: CategoryId,
  val amount: Amount,
  val isIncome: Boolean,
  val isHidden: Boolean,
  val isGroupHidden: Boolean,
)

data class CategoryBudget(
  val month: YearMonth,
  val category: CategoryId,
  val amount: Amount,
  val carryover: Boolean,
)

// The stored values of one budget row
data class BudgetRow(val id: String, val amount: Amount, val carryover: Boolean)

private const val YEAR_MONTH_FACTOR = 100

@Inject
class BudgetDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.budgetsQueries

  fun observeEarliestTransactionDate(): Flow<LocalDate?> =
    queries
      .budgetEarliestTransactionDate()
      .asFlow()
      .mapToOne(contexts.default)
      .map { it.date }
      .distinctUntilChanged()

  // Live categories in live groups, income groups last
  fun observeCategories(): Flow<List<BudgetCategories>> =
    queries.budgetCategories().asFlow().mapToList(contexts.default).distinctUntilChanged()

  // Live groups in the same order as observeCategories(), empty ones included
  fun observeCategoryGroups(): Flow<List<BudgetCategoryGroups>> =
    queries.budgetCategoryGroups().asFlow().mapToList(contexts.default).distinctUntilChanged()

  // Per-category totals of on-budget transactions for each month in the range
  fun observeSpentByMonth(start: LocalDate, end: LocalDate): Flow<List<BudgetSpentByMonth>> =
    queries
      .budgetSpentByMonth(start, end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeEnvelopeBudgets(start: YearMonth, end: YearMonth): Flow<List<CategoryBudget>> =
    queries
      .zeroBudgets(start, end, ::categoryBudget)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeTrackingBudgets(start: YearMonth, end: YearMonth): Flow<List<CategoryBudget>> =
    queries
      .reflectBudgets(start, end, ::categoryBudget)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeEnvelopeMonths(): Flow<List<Zero_budget_months>> =
    queries.zeroBudgetMonths().asFlow().mapToList(contexts.default).distinctUntilChanged()

  suspend fun categories(): List<BudgetCategories> = queries.withResult {
    budgetCategories().awaitAsList()
  }

  suspend fun spentByMonth(start: LocalDate, end: LocalDate): List<BudgetSpentByMonth> =
    queries.withResult {
      budgetSpentByMonth(start, end).awaitAsList()
    }

  // Upstream looks up the row's ID, which may not be "${YYYYMM}-${category}"
  suspend fun budgetRow(type: BudgetType, month: YearMonth, category: CategoryId): BudgetRow? =
    queries.withResult {
      when (type) {
        Envelope -> zeroBudgetRow(month, category, ::budgetRow)
        Tracking -> reflectBudgetRow(month, category, ::budgetRow)
      }.awaitAsOneOrNull()
    }

  // Rows of live categories only
  suspend fun budgetsInMonth(type: BudgetType, month: YearMonth): List<MonthBudget> =
    queries.withResult {
      when (type) {
        Envelope -> zeroBudgetsInMonth(month, ::monthBudget)
        Tracking -> reflectBudgetsInMonth(month, ::monthBudget)
      }.awaitAsList()
    }

  // The earliest month up to [end] with a budget row or an on-budget transaction for [category]
  suspend fun firstActivityMonth(
    type: BudgetType,
    category: CategoryId,
    end: YearMonth,
  ): YearMonth? =
    queries
      .withResult {
        when (type) {
          Envelope -> zeroBudgetFirstActivity(category, end).awaitAsOneOrNull()?.month
          Tracking -> reflectBudgetFirstActivity(category, end).awaitAsOneOrNull()?.month
        }
      }
      ?.let {
        YearMonth(year = (it / YEAR_MONTH_FACTOR).toInt(), month = (it % YEAR_MONTH_FACTOR).toInt())
      }

  private fun budgetRow(id: String, amount: Amount?, carryover: Long?) =
    BudgetRow(id = id, amount = amount ?: Zero, carryover = carryover == 1L)

  @Suppress("CanBeNonNullable")
  private fun monthBudget(
    category: CategoryId?,
    amount: Amount?,
    isIncome: Boolean?,
    isHidden: Boolean?,
    isGroupHidden: Boolean?,
  ) =
    MonthBudget(
      category = requireNotNull(category),
      amount = amount ?: Zero,
      isIncome = isIncome == true,
      isHidden = isHidden == true,
      isGroupHidden = isGroupHidden == true,
    )

  @Suppress("CanBeNonNullable")
  private fun categoryBudget(
    month: YearMonth?,
    category: CategoryId,
    amount: Amount?,
    carryover: Boolean,
  ) =
    CategoryBudget(
      month = requireNotNull(month),
      category = category,
      amount = amount ?: Zero,
      carryover = carryover,
    )
}
