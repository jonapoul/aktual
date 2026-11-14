package aktual.budget.reports.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.BudgetType
import aktual.budget.model.SyncedPrefKey
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.ConditionEntitiesLoader
import aktual.budget.rules.domain.ConditionEntity
import aktual.core.utils.Calendar
import androidx.compose.runtime.Immutable
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.minus
import kotlinx.datetime.yearMonth

// What the query editor picks from
@Immutable
data class QueryEditorData(
  override val accounts: ImmutableList<ConditionEntity>,
  override val categories: ImmutableList<ConditionEntity>,
  override val categoryGroups: ImmutableList<ConditionEntity>,
  override val payees: ImmutableList<ConditionEntity>,
  val currentMonth: YearMonth,
  val months: YearMonthRange,
  override val names: ImmutableMap<String, String>,
  // Every category by group, hidden and income ones included
  val categoryChoices: ImmutableList<CategoryGroupChoice> = persistentListOf(),
  // Closed accounts included
  val allAccounts: ImmutableList<ConditionEntity> = persistentListOf(),
  val isTrackingBudget: Boolean = false,
) : ConditionEntities

@Inject
internal class QueryEditorDataLoader(
  database: BudgetDatabase,
  private val entityLoader: ConditionEntitiesLoader,
  private val reports: ReportsDao,
  private val calendar: Calendar,
) {
  private val accounts = AccountDao(database)
  private val budgets = database.budgetsQueries
  private val preferences = database.preferencesQueries

  // Months from the first transaction, or at least a year back, like upstream's query editor
  fun load(): Flow<QueryEditorData> =
    reports.observeTransactionDateBounds().map { bounds ->
      val current = calendar.today().yearMonth
      val latest = maxOf(bounds.latest?.yearMonth ?: current, current)
      val earliest = minOf(bounds.earliest?.yearMonth ?: current, latest.minus(MIN_MONTHS, MONTH))
      val lists = entityLoader.load()
      QueryEditorData(
        accounts = lists.accounts,
        categories = lists.categories,
        categoryGroups = lists.categoryGroups,
        payees = lists.payees,
        currentMonth = current,
        months = YearMonthRange(earliest, latest),
        names = lists.names,
        categoryChoices =
          categoryGroupChoices(
            groups = budgets.budgetCategoryGroups().awaitAsList(),
            categories = budgets.budgetCategories().awaitAsList(),
          ),
        allAccounts = accounts.getAllWithStatus().entities({ it.id.toString() }, { it.name }),
        isTrackingBudget = BudgetType.from(budgetType()) == Tracking,
      )
    }

  private suspend fun budgetType(): String? =
    preferences.getValue(SyncedPrefKey.Global.BudgetType).awaitAsOneOrNull()?.value_

  private inline fun <T> List<T>.entities(
    id: (T) -> String,
    name: (T) -> String?,
  ): ImmutableList<ConditionEntity> = mapNotNull { row ->
    name(row)?.let { ConditionEntity(id(row), it) }
  }
    .toImmutableList()

  private companion object {
    const val MIN_MONTHS = 12
  }
}
