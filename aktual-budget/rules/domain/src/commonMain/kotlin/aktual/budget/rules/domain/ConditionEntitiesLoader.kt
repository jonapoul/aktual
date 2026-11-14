package aktual.budget.rules.domain

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.CategoryGroupDao
import aktual.budget.db.dao.PayeeDao
import androidx.compose.runtime.Immutable
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

@Immutable
data class ConditionEntityLists(
  override val accounts: ImmutableList<ConditionEntity>,
  override val categories: ImmutableList<ConditionEntity>,
  override val categoryGroups: ImmutableList<ConditionEntity>,
  override val payees: ImmutableList<ConditionEntity>,
  override val names: ImmutableMap<String, String>,
) : ConditionEntities

@Inject
class ConditionEntitiesLoader(database: BudgetDatabase) {
  private val accounts = AccountDao(database)
  private val categories = CategoryDao(database)
  private val categoryGroups = CategoryGroupDao(database)
  private val payees = PayeeDao(database)

  suspend fun load(): ConditionEntityLists {
    val activePayees = payees.getAllActive()
    return ConditionEntityLists(
      accounts = accounts.getAllActive().entities({ it.id.toString() }, { it.name }),
      categories = categories.getAllActive().entities({ it.id.toString() }, { it.name }),
      categoryGroups = categoryGroups.getAllActive().entities({ it.id.toString() }, { it.name }),
      payees = activePayees.entities({ it.id.toString() }, { it.name }),
      names = buildMap {
          accounts.nameMap().forEach { (id, name) -> name?.let { put(id.toString(), it) } }
          categories.nameMap().forEach { (id, name) -> name?.let { put(id.toString(), it) } }
          categoryGroups.nameMap().forEach { (id, name) ->
            name?.let { put(id.toString(), it) }
          }
          activePayees.forEach { row -> row.name?.let { put(row.id.toString(), it) } }
        }
          .toImmutableMap(),
    )
  }

  private inline fun <T> List<T>.entities(
    id: (T) -> String,
    name: (T) -> String?,
  ): ImmutableList<ConditionEntity> = mapNotNull { row ->
    name(row)?.let { ConditionEntity(id(row), it) }
  }
    .toImmutableList()
}
