package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.BudgetWriter
import aktual.budget.budgeting.domain.CategoryOrder
import aktual.budget.budgeting.domain.CategoryWriter
import aktual.budget.budgeting.domain.NameResult
import aktual.budget.budgeting.domain.OrderedGroup
import aktual.budget.budgeting.domain.UndoToken
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The add, rename and delete dialogs for categories and groups, and hiding groups. Upstream's
 * mobile budget page does these through NewCategoryModal, NewCategoryGroupModal, CategoryMenuModal,
 * CategoryGroupMenuModal and ConfirmCategoryDeleteModal.
 */
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class CategoryEditViewModel
internal constructor(
  private val categoryWriter: CategoryWriter,
  private val budgetWriter: BudgetWriter,
  private val order: CategoryOrder,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableDialog = MutableStateFlow<CategoryDialog?>(null)
  val dialog: StateFlow<CategoryDialog?> = mutableDialog.asStateFlow()

  private val mutableEvents =
    MutableSharedFlow<CategoryEditEvent>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
  val events: SharedFlow<CategoryEditEvent> = mutableEvents.asSharedFlow()

  // Only the latest change can be undone
  private var lastUndo: UndoToken? = null

  fun startAdd() {
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val groups = order.load().map { GroupChoice(it.id, it.name, it.isIncome) }
          mutableDialog.update { CategoryDialog.Add(groups.toImmutableList()) }
        },
        onFailure = { e -> log.e(e) { "Failed loading groups" } },
      )
    }
  }

  fun startRename(target: CategoryTarget) = mutableDialog.update { CategoryDialog.Rename(target) }

  fun startDelete(target: CategoryTarget) {
    mutableDialog.update { CategoryDialog.Delete(target, Checking) }
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val transfer = transferFor(target)
          updateDialog<CategoryDialog.Delete> { it.copy(transfer = transfer) }
        },
        onFailure = { e ->
          log.e(e) { "Failed checking whether $target needs a transfer" }
          updateDialog<CategoryDialog.Delete> { it.copy(status = Failed) }
        },
      )
    }
  }

  fun dismiss() = mutableDialog.update { null }

  // Clears a duplicate or failure once the input changes
  fun edited() = mutableDialog.update { dialog ->
    when (dialog) {
      is CategoryDialog.Add -> dialog.copy(status = dialog.status.edited())
      is CategoryDialog.Rename -> dialog.copy(status = dialog.status.edited())
      is CategoryDialog.Delete -> dialog.copy(status = dialog.status.edited())
      null -> null
    }
  }

  // A null group is only valid for a new group
  fun create(kind: AddKind, group: CategoryGroupId?, name: String) =
    saveName<CategoryDialog.Add>(
      update = { dialog, status -> dialog.copy(status = status) },
      action = {
        when (kind) {
          NewCategory -> categoryWriter.createCategory(requireNotNull(group), name)
          NewGroup -> categoryWriter.createGroup(name)
        }
      },
    )

  fun rename(name: String) {
    val target = (mutableDialog.value as? CategoryDialog.Rename)?.target ?: return
    saveName<CategoryDialog.Rename>(
      update = { dialog, status -> dialog.copy(status = status) },
      action = {
        when (target) {
          is Category -> categoryWriter.renameCategory(target.id, name)
          is Group -> categoryWriter.renameGroup(target.id, name)
        }
      },
    )
  }

  // [transferTo] is needed when the dialog asks for one
  fun delete(transferTo: CategoryId?) {
    val dialog = mutableDialog.value as? CategoryDialog.Delete ?: return
    if (dialog.status == Saving) return
    if (dialog.transfer is Needed && transferTo == null) return
    if (dialog.transfer == Checking) return
    updateDialog<CategoryDialog.Delete> { it.copy(status = Saving) }
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val token =
            when (val target = dialog.target) {
              is Category -> categoryWriter.deleteCategory(target.id, transferTo)
              is Group -> categoryWriter.deleteGroup(target.id, transferTo)
            }
          lastUndo = token
          mutableDialog.update { null }
          mutableEvents.tryEmit(CategoryEditEvent.Deleted(token))
        },
        onFailure = { e ->
          log.e(e) { "Failed deleting ${dialog.target}" }
          updateDialog<CategoryDialog.Delete> { it.copy(status = Failed) }
        },
      )
    }
  }

  fun setGroupHidden(id: CategoryGroupId, hidden: Boolean) {
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val token = categoryWriter.setGroupHidden(id, hidden)
          lastUndo = token
          mutableEvents.tryEmit(CategoryEditEvent.Updated(token))
        },
        onFailure = { e -> log.e(e) { "Failed to set $id hidden = $hidden" } },
      )
    }
  }

  // Does nothing once a later change has been made
  fun undo(token: UndoToken) {
    if (lastUndo !== token) return
    lastUndo = null
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = { budgetWriter.undo(token) },
        onFailure = { e -> log.e(e) { "Failed to undo" } },
      )
    }
  }

  private inline fun <reified D : CategoryDialog> saveName(
    crossinline update: (D, DialogStatus) -> D,
    crossinline action: suspend () -> NameResult<*>,
  ) {
    val dialog = mutableDialog.value as? D ?: return
    if (dialog.status == Saving) return
    updateDialog<D> { update(it, Saving) }
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          when (action()) {
            is Saved<*> -> mutableDialog.update { null }
            Duplicate -> updateDialog<D> { update(it, Duplicate) }
            // The dialogs don't allow blank names
            Blank -> updateDialog<D> { update(it, Editing) }
          }
        },
        onFailure = { e ->
          log.e(e) { "Failed saving $dialog" }
          updateDialog<D> { update(it, Failed) }
        },
      )
    }
  }

  private inline fun <reified D : CategoryDialog> updateDialog(crossinline block: (D) -> D) =
    mutableDialog.update { dialog ->
      if (dialog is D) block(dialog) else dialog
    }

  private suspend fun transferFor(target: CategoryTarget): Transfer {
    val groups = order.load()
    val (deleted, isIncome) =
      when (target) {
        is Category -> {
          val group = groups.first { g -> g.categories.any { it.id == target.id } }
          setOf(target.id) to group.isIncome
        }

        is Group -> {
          val group = groups.first { it.id == target.id }
          group.categories.map { it.id }.toSet() to group.isIncome
        }
      }
    if (!categoryWriter.isTransferRequired(deleted)) return NotNeeded
    return Transfer.Needed(groups.transferGroups(deleted, isIncome))
  }

  private fun List<OrderedGroup>.transferGroups(deleted: Set<CategoryId>, isIncome: Boolean) =
    filter {
      it.isIncome == isIncome
    }
    .map { group ->
      TransferGroup(
        name = group.name,
        categories =
          group.categories
            .filter { it.id !in deleted }
            .map { TransferCategory(it.id, it.name) }
            .toImmutableList(),
      )
    }
    .filter { it.categories.isNotEmpty() }
    .toImmutableList()

  private fun DialogStatus.edited(): DialogStatus = if (this == Saving) this else Editing
}
