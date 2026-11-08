package aktual.budget.budgeting.vm

import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ReorderCategoriesState {
  data object Loading : ReorderCategoriesState

  data object Failed : ReorderCategoriesState

  data class Loaded(
    // Expense groups first, then income ones
    val groups: ImmutableList<ReorderGroup>,
    val hasChanges: Boolean,
    val isSaving: Boolean,
  ) : ReorderCategoriesState
}

@Immutable
data class ReorderGroup(
  val id: CategoryGroupId,
  val name: String,
  val isIncome: Boolean,
  val isHidden: Boolean,
  val categories: ImmutableList<ReorderCategory>,
)

@Immutable data class ReorderCategory(val id: CategoryId, val name: String, val isHidden: Boolean)

sealed interface ReorderCategoriesEvent {
  data object Saved : ReorderCategoriesEvent

  data object SaveFailed : ReorderCategoriesEvent
}
