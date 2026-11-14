package aktual.budget.transactions.vm

import aktual.budget.transactions.domain.SavedFilter
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// [applied] is the saved filter last applied or saved, and [isModified] whether the conditions have
// changed since
@Immutable
data class SavedFiltersState(
  val filters: ImmutableList<SavedFilter> = persistentListOf(),
  val applied: SavedFilter? = null,
  val isModified: Boolean = false,
)
