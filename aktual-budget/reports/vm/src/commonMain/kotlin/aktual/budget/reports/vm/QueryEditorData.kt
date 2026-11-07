package aktual.budget.reports.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.CategoryGroupDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.BudgetType
import aktual.budget.model.Field
import aktual.budget.model.SyncedPrefKey
import aktual.core.utils.Calendar
import androidx.compose.runtime.Immutable
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.minus
import kotlinx.datetime.yearMonth

@Immutable data class QueryEntity(val id: String, val name: String)

// What the query editor picks from
@Immutable
data class QueryEditorData(
  val accounts: ImmutableList<QueryEntity>,
  val categories: ImmutableList<QueryEntity>,
  val categoryGroups: ImmutableList<QueryEntity>,
  val payees: ImmutableList<QueryEntity>,
  val currentMonth: YearMonth,
  val months: YearMonthRange,
  // Every entity's name by id, including hidden categories and closed accounts that saved
  // conditions can still point to
  val names: ImmutableMap<String, String>,
  // Every category by group, hidden and income ones included
  val categoryChoices: ImmutableList<CategoryGroupChoice> = persistentListOf(),
  // Closed accounts included
  val allAccounts: ImmutableList<QueryEntity> = persistentListOf(),
  val isTrackingBudget: Boolean = false,
)

fun QueryEditorData.entities(field: Field): ImmutableList<QueryEntity> =
  when (field) {
    Account,
    Acct -> accounts
    Category -> categories
    CategoryGroup -> categoryGroups
    Payee,
    Description -> payees
    Amount,
    Date,
    Notes,
    PayeeName,
    ImportedDescription,
    ImportedPayee,
    Saved,
    Transfer,
    Parent,
    Cleared,
    Reconciled,
    Unknown -> persistentListOf()
  }

@Inject
internal class QueryEditorDataLoader(
  database: BudgetDatabase,
  private val reports: ReportsDao,
  private val calendar: Calendar,
) {
  private val accounts = AccountDao(database)
  private val categories = CategoryDao(database)
  private val categoryGroups = CategoryGroupDao(database)
  private val payees = PayeeDao(database)
  private val budgets = database.budgetsQueries
  private val preferences = database.preferencesQueries

  // Months from the first transaction, or at least a year back, like upstream's query editor
  fun load(): Flow<QueryEditorData> =
    reports.observeTransactionDateBounds().map { bounds ->
      val current = calendar.today().yearMonth
      val latest = maxOf(bounds.latest?.yearMonth ?: current, current)
      val earliest = minOf(bounds.earliest?.yearMonth ?: current, latest.minus(MIN_MONTHS, MONTH))
      QueryEditorData(
        accounts = accounts.getAllActive().entities({ it.id.toString() }, { it.name }),
        categories = categories.getAllActive().entities({ it.id.toString() }, { it.name }),
        categoryGroups = categoryGroups.getAllActive().entities({ it.id.toString() }, { it.name }),
        payees = payees.getAllActive().entities({ it.id.toString() }, { it.name }),
        currentMonth = current,
        months = YearMonthRange(earliest, latest),
        names = names(),
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

  private suspend fun names(): ImmutableMap<String, String> = buildMap {
    accounts.nameMap().forEach { (id, name) -> name?.let { put(id.toString(), it) } }
    categories.nameMap().forEach { (id, name) -> name?.let { put(id.toString(), it) } }
    categoryGroups.nameMap().forEach { (id, name) -> name?.let { put(id.toString(), it) } }
    payees.getAllActive().forEach { row -> row.name?.let { put(row.id.toString(), it) } }
  }
    .toImmutableMap()

  private inline fun <T> List<T>.entities(
    id: (T) -> String,
    name: (T) -> String?,
  ): ImmutableList<QueryEntity> = mapNotNull { row ->
    name(row)?.let { QueryEntity(id(row), it) }
  }
    .toImmutableList()

  private companion object {
    const val MIN_MONTHS = 12
  }
}
