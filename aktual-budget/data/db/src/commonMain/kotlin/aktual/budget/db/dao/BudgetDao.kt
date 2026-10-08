package aktual.budget.db.dao

import aktual.budget.db.BudgetCategories
import aktual.budget.db.BudgetCategoryGroups
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.BudgetSpentByMonth
import aktual.budget.db.Zero_budget_months
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

data class CategoryBudget(
  val month: YearMonth,
  val category: CategoryId,
  val amount: Amount,
  val carryover: Boolean,
)

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
