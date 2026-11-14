package aktual.budget.transactions.domain

import aktual.budget.db.DbJson
import aktual.budget.db.Transaction_filters
import aktual.budget.db.dao.DatabaseTables.TRANSACTION_FILTERS
import aktual.budget.db.dao.TransactionFilterDao
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOptions
import aktual.budget.model.ConditionSet
import aktual.budget.model.TransactionFilterId
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.utils.BudgetSyncController
import aktual.core.utils.UuidGenerator
import androidx.compose.runtime.Immutable
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

@Immutable
data class SavedFilter(val id: TransactionFilterId, val name: String, val conditions: ConditionSet)

@Immutable
sealed interface SaveFilterResult {
  @JvmInline value class Saved(val id: TransactionFilterId) : SaveFilterResult

  data object NameRequired : SaveFilterResult

  @JvmInline value class NameTaken(val name: String) : SaveFilterResult

  // Another filter already has these conditions
  @JvmInline value class ConditionsTaken(val name: String) : SaveFilterResult
}

/**
 * The transaction filters saved in the budget, synced like upstream's. Saving checks what
 * createFilter and updateFilter in packages/loot-core/src/server/filters/app.ts check: the name is
 * required and unique, and no other filter has the same conditions
 */
@Inject
class SavedFilters
internal constructor(
  private val dao: TransactionFilterDao,
  private val syncController: BudgetSyncController,
  private val uuidGenerator: UuidGenerator,
) {
  fun observe(): Flow<ImmutableList<SavedFilter>> =
    dao.observeAllActive().map { rows -> rows.map { it.toSavedFilter() }.toImmutableList() }

  suspend fun create(name: String, conditions: ConditionSet): SaveFilterResult {
    val id = uuidGenerator(::TransactionFilterId)
    val failure = validate(id = null, name.trim(), conditions)
    if (failure != null) return failure
    syncController.syncChanges(
      listOf(localChange(TRANSACTION_FILTERS, id.value, "id", id.value)) +
        changes(id, name.trim(), conditions) +
        localChange(TRANSACTION_FILTERS, id.value, "tombstone", false),
    )
    return SaveFilterResult.Saved(id)
  }

  suspend fun update(
    id: TransactionFilterId,
    name: String,
    conditions: ConditionSet,
  ): SaveFilterResult {
    val failure = validate(id, name.trim(), conditions)
    if (failure != null) return failure
    syncController.syncChanges(changes(id, name.trim(), conditions))
    return SaveFilterResult.Saved(id)
  }

  suspend fun delete(id: TransactionFilterId) =
    syncController.syncChanges(listOf(tombstone(TRANSACTION_FILTERS, id.value)))

  private suspend fun validate(
    id: TransactionFilterId?,
    name: String,
    conditions: ConditionSet,
  ): SaveFilterResult? {
    if (name.isEmpty()) return SaveFilterResult.NameRequired
    val others = dao.getAllActive().map { it.toSavedFilter() }.filter { it.id != id }
    if (others.any { it.name == name }) return SaveFilterResult.NameTaken(name)
    val same = others.firstOrNull { it.conditions.sameAs(conditions) }
    return same?.let { SaveFilterResult.ConditionsTaken(it.name) }
  }

  private fun changes(id: TransactionFilterId, name: String, conditions: ConditionSet) =
    listOf(
      localChange(TRANSACTION_FILTERS, id.value, "name", name),
      localChange(
        TRANSACTION_FILTERS,
        id.value,
        "conditions",
        DbJson.encodeToString(conditions.conditions),
      ),
      localChange(TRANSACTION_FILTERS, id.value, "conditions_op", conditions.op),
    )

  private fun Transaction_filters.toSavedFilter() =
    SavedFilter(
      id = id,
      name = name.orEmpty(),
      conditions = ConditionSet(conditions.orEmpty(), conditions_op ?: And),
    )

  // conditionExists in upstream's filters app. Values are compared with ===, so a list or object
  // value never matches another
  private fun ConditionSet.sameAs(other: ConditionSet): Boolean =
    (other.conditions.size == 1 || op == other.op) &&
      conditions.size == other.conditions.size &&
      other.conditions.all { condition -> conditions.any { it.sameAs(condition) } }

  private fun Condition.sameAs(other: Condition): Boolean =
    value !is JsonArray &&
      value !is JsonObject &&
      value == other.value &&
      operator == other.operator &&
      field == other.field &&
      options.orEmpty() == other.options.orEmpty()

  private fun ConditionOptions?.orEmpty() = this ?: ConditionOptions()
}
