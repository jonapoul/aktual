package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.CustomReportAccounts
import aktual.budget.db.CustomReportAmounts
import aktual.budget.db.CustomReportBudgets
import aktual.budget.db.CustomReportCategories
import aktual.budget.db.CustomReportCategoryGroups
import aktual.budget.db.CustomReportPayees
import aktual.budget.db.CustomReportTransactions
import aktual.budget.db.CustomReports
import aktual.budget.db.withResult
import aktual.budget.model.CustomReportId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

@Inject
class CustomReportsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.customReportsQueries

  suspend fun insert(reports: CustomReports): Long = queries.withResult { insert(reports) }

  suspend operator fun get(id: CustomReportId): CustomReports? = queries.withResult {
    getById(id).awaitAsOneOrNull()
  }

  fun observe(id: CustomReportId): Flow<CustomReports?> =
    queries.getById(id).asFlow().mapToOneOrNull(contexts.default).distinctUntilChanged()

  fun observeNames(): Flow<Map<CustomReportId, String>> =
    queries
      .getNames()
      .asFlow()
      .mapToList(contexts.default)
      .map { rows -> rows.associate { it.id to it.name } }
      .distinctUntilChanged()

  suspend fun getIds(): List<CustomReportId> = queries.withResult { getIds().awaitAsList() }

  suspend fun getIdByName(name: String): CustomReportId? = queries.withResult {
    getIdByName(name).awaitAsOneOrNull()
  }

  fun observeAmounts(
    start: LocalDate,
    end: LocalDate,
    divisor: Long,
  ): Flow<List<CustomReportAmounts>> =
    queries
      .customReportAmounts(divisor, start, end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeTransactions(
    start: LocalDate,
    end: LocalDate,
    divisor: Long,
  ): Flow<List<CustomReportTransactions>> =
    queries
      .customReportTransactions(divisor, start, end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeBudgets(
    start: YearMonth,
    end: YearMonth,
    divisor: Long,
    isTracking: Boolean,
  ): Flow<List<CustomReportBudgets>> =
    queries
      .customReportBudgets(
        divisor = divisor,
        tracking = if (isTracking) 1L else 0L,
        start = start.toLong(),
        end = end.toLong(),
      )
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeCategories(): Flow<List<CustomReportCategories>> =
    queries.customReportCategories().asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeCategoryGroups(): Flow<List<CustomReportCategoryGroups>> =
    queries.customReportCategoryGroups().asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observePayees(): Flow<List<CustomReportPayees>> =
    queries.customReportPayees().asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeAccounts(): Flow<List<CustomReportAccounts>> =
    queries.customReportAccounts().asFlow().mapToList(contexts.default).distinctUntilChanged()
}

private fun YearMonth.toLong(): Long = year * YEAR_MONTH_FACTOR + month.number

private const val YEAR_MONTH_FACTOR = 100L
