package aktual.budget.accounts.vm.edit

import aktual.budget.accounts.domain.AccountUndo
import aktual.budget.accounts.domain.AccountWriter
import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.db.GetManageList
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.AccountGroupDao
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.transactions.domain.Patch
import aktual.budget.transactions.domain.Patch.Keep
import aktual.budget.transactions.domain.Patch.To
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The edit sheet: an account's name, notes and group. Names stay unique among accounts that aren't
 * deleted, closed ones included, as validateAccountName().
 */
@Stable
@AssistedInject
class EditAccountViewModel
internal constructor(
  @Assisted private val accountId: AccountId,
  private val writer: AccountWriter,
  accountDao: AccountDao,
  groupDao: AccountGroupDao,
  private val budgetScope: BudgetCoroutineScope,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val original = MutableStateFlow<Original?>(null)
  private val mutableName = MutableStateFlow("")
  private val mutableNotes = MutableStateFlow("")
  private val mutableGroup = MutableStateFlow<AccountGroupId?>(null)
  private val isSaving = MutableStateFlow(false)
  private val saveError = MutableStateFlow<String?>(null)
  private val loadError = MutableStateFlow<String?>(null)

  private val mutableEvents =
    MutableSharedFlow<EditAccountEvent>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
  val events: SharedFlow<EditAccountEvent> = mutableEvents.asSharedFlow()

  private val source: StateFlow<Source?> =
    combine(accountDao.observeManageList(), groupDao.observeAllActive()) { rows, groups ->
        val account = rows.firstOrNull { it.id == accountId } ?: error("No account $accountId")
        Source(
          account = account,
          otherNames =
            rows.mapNotNullTo(HashSet()) { row -> row.name.takeIf { row.id != accountId } },
          groups = groups.map { AccountGroupItem(it.id, it.name.orEmpty()) }.toImmutableList(),
        )
      }
      .onEach(::seed)
      .catch { e ->
        log.e(e) { "Failed loading account $accountId" }
        loadError.update { e.requireMessage() }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  private val edits =
    combine(original, mutableName, mutableNotes, mutableGroup) { original, name, notes, group ->
      Edits(original, name, notes, group)
    }

  val state: StateFlow<EditAccountState> =
    combine(source, loadError, edits, isSaving, saveError) { source, error, edits, saving, saveError
        ->
        when {
          error != null -> Failure(error)
          source == null || edits.original == null -> Loading
          else -> editing(source, edits, saving, saveError)
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun setName(name: String) = mutableName.update { name }

  fun setNotes(notes: String) = mutableNotes.update { notes }

  fun setGroup(group: AccountGroupId?) = mutableGroup.update { group }

  fun dismissSaveError() = saveError.update { null }

  fun save() {
    // Not state.value, which may not have caught up with the latest input yet
    val source = source.value ?: return
    val edits = Edits(original.value, mutableName.value, mutableNotes.value, mutableGroup.value)
    val original = edits.original ?: return
    if (!canSave(source, edits, isSaving.value)) return
    val name = edits.name.trim()
    val namePatch = patch(original.name, name)
    isSaving.update { true }

    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val undo =
            writer.edit(
              accountId,
              name = namePatch,
              notes = patch(original.notes, edits.notes),
              group = patch(original.group, edits.group),
            )
          // Stays saving, so it can't be saved twice before the sheet closes
          val renamedFrom = original.name.takeIf { namePatch is To }
          mutableEvents.tryEmit(EditAccountEvent.Saved(name, renamedFrom, undo))
        },
        onFailure = { e ->
          log.e(e) { "Failed saving account $accountId" }
          saveError.update { e.requireMessage() }
          isSaving.update { false }
        },
      )
    }
  }

  // The undo snackbar outlives the sheet, so the undo can't be tied to viewModelScope
  fun undo(undo: AccountUndo) {
    budgetScope.launch {
      runCatchingCancellable(
        attempt = { writer.undo(undo) },
        onFailure = { e -> log.e(e) { "Failed undoing edit of $accountId" } },
      )
    }
  }

  // Seeds the working edits once, so later reloads don't overwrite what's been typed
  private suspend fun seed(source: Source) {
    if (original.value != null) return
    val account = source.account
    val notes = writer.notes(accountId)
    val name = account.name.orEmpty()
    mutableName.update { name }
    mutableNotes.update { notes }
    mutableGroup.update { account.account_group_id }
    original.update { Original(name, notes, account.account_group_id) }
  }

  private fun editing(source: Source, edits: Edits, saving: Boolean, error: String?): Editing {
    val trimmed = edits.name.trim()
    return Editing(
      name = edits.name,
      notes = edits.notes,
      group = edits.group,
      groups = source.groups,
      offBudget = source.account.offbudget == true,
      balance = Amount(source.account.balance),
      transactionCount = source.account.transaction_count,
      duplicateName = trimmed.takeIf { it in source.otherNames },
      canSave = canSave(source, edits, saving),
      isSaving = saving,
      saveError = error,
    )
  }

  private fun canSave(source: Source, edits: Edits, saving: Boolean): Boolean {
    val original = edits.original ?: return false
    val trimmed = edits.name.trim()
    val changed =
      trimmed != original.name || edits.notes != original.notes || edits.group != original.group
    return !saving && changed && trimmed.isNotEmpty() && trimmed !in source.otherNames
  }

  private fun <T> patch(old: T, new: T): Patch<T> = if (old == new) Keep else To(new)

  private data class Original(val name: String, val notes: String, val group: AccountGroupId?)

  private data class Edits(
    val original: Original?,
    val name: String,
    val notes: String,
    val group: AccountGroupId?,
  )

  private data class Source(
    val account: GetManageList,
    val otherNames: Set<String>,
    val groups: ImmutableList<AccountGroupItem>,
  )

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(accountId: AccountId): EditAccountViewModel
  }
}
