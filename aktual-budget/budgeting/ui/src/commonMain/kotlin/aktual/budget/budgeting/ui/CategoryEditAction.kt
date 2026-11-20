package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.CategoryEditViewModel
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline

@Immutable
internal sealed interface CategoryEditAction {
  data object Dismiss : CategoryEditAction

  // The input changed, clearing a duplicate or failure
  data object Edited : CategoryEditAction

  data class CreateCategory(val group: CategoryGroupId, val name: String) : CategoryEditAction

  @JvmInline value class CreateGroup(val name: String) : CategoryEditAction

  @JvmInline value class Rename(val name: String) : CategoryEditAction

  // Null when the dialog doesn't ask for a transfer
  @JvmInline value class Delete(val transferTo: CategoryId?) : CategoryEditAction
}

internal fun CategoryEditViewModel.onAction(action: CategoryEditAction) {
  when (action) {
    Dismiss -> dismiss()
    Edited -> edited()
    is CreateCategory -> createCategory(action.group, action.name)
    is CreateGroup -> createGroup(action.name)
    is Rename -> rename(action.name)
    is Delete -> delete(action.transferTo)
  }
}
