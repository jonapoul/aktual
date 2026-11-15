package aktual.budget.transactions.domain

import aktual.budget.model.ConditionSet
import aktual.budget.model.TransactionFilterId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

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
interface SavedFilters {
  fun observe(): Flow<ImmutableList<SavedFilter>>

  suspend fun create(name: String, conditions: ConditionSet): SaveFilterResult

  suspend fun update(
    id: TransactionFilterId,
    name: String,
    conditions: ConditionSet,
  ): SaveFilterResult

  suspend fun delete(id: TransactionFilterId)
}
