package aktual.budget.transactions.vm.edit

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.transactions.domain.LoadedTransaction
import aktual.budget.transactions.domain.TransactionFields
import aktual.budget.transactions.domain.TransactionLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.budget.transactions.domain.transactionDiff
import aktual.budget.transactions.vm.edit.EditTransactionState.Failure
import aktual.budget.transactions.vm.toTransaction
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import logcat.logcat

@Stable
@AssistedInject
class EditTransactionViewModel
internal constructor(
  @Assisted private val id: TransactionId,
  private val loader: TransactionLoader,
  private val writer: TransactionWriter,
  private val accountDao: AccountDao,
  private val payeeDao: PayeeDao,
  private val categoryDao: CategoryDao,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted id: TransactionId): EditTransactionViewModel
  }

  // What's currently saved, following any changes made elsewhere, e.g. by a sync
  private val mutableSaved = MutableStateFlow<Saved?>(null)
  private val mutableEntities = MutableStateFlow<Entities?>(null)
  private val mutableFailure = MutableStateFlow<Failure?>(null)
  // The edits in progress, or null when viewing
  private val mutableDraft = MutableStateFlow<TransactionFields?>(null)
  private val mutableIsWorking = MutableStateFlow(false)
  private val mutableError = MutableStateFlow<EditTransactionError?>(null)
  private var isDeleted = false

  private val mutableEvents =
    MutableSharedFlow<EditTransactionEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<EditTransactionEvent> = mutableEvents.asSharedFlow()

  // The reason the last save or delete failed, shown in a dialog
  val error: StateFlow<EditTransactionError?> = mutableError.asStateFlow()

  val state: StateFlow<EditTransactionState> =
    viewModelScope.launchMolecule(Immediate) {
      val saved by mutableSaved.collectAsState()
      val entities by mutableEntities.collectAsState()
      val failure by mutableFailure.collectAsState()
      val draft by mutableDraft.collectAsState()
      val isWorking by mutableIsWorking.collectAsState()
      buildState(saved, entities, failure, draft, isWorking)
    }

  init {
    viewModelScope.launch { observe() }
  }

  private fun buildState(
    saved: Saved?,
    entities: Entities?,
    failure: Failure?,
    draft: TransactionFields?,
    isWorking: Boolean,
  ): EditTransactionState {
    if (failure != null) return failure
    if (saved == null || entities == null) return Loading
    val mode: TransactionEditMode = if (draft == null) View else editMode(saved, entities, draft)
    return EditTransactionState.Loaded(
      saved = saved.details,
      mode = mode,
      canEdit = saved.canEdit,
      isWorking = isWorking,
    )
  }

  // A name the pickers don't offer, like a closed account's, falls back to the saved one
  private fun editMode(
    saved: Saved,
    entities: Entities,
    draft: TransactionFields,
  ): TransactionEditMode.Edit {
    val transaction = saved.details.transaction
    return TransactionEditMode.Edit(
      draft = draft,
      payeeName =
        draft.payee?.let { id ->
          entities.payeeNames[id] ?: transaction.payee.takeIf { id == saved.fields.payee }
        },
      categoryName =
        draft.category?.let { id ->
          entities.categoryNames[id] ?: transaction.category.takeIf { id == saved.fields.category }
        },
      accountName = draft.account?.let(entities.accountNames::get),
      isOffBudget = draft.account in entities.offBudget,
      hasChanges = transactionDiff(transaction.id, saved.fields, draft) != null,
      options = entities.options,
    )
  }

  private suspend fun observe() {
    try {
      mutableEntities.update { loadEntities() }
      loader.observe(id).collect { loaded ->
        if (loaded != null) {
          mutableFailure.update { null }
          mutableSaved.update { loaded.toSaved() }
        } else if (!isDeleted) {
          mutableFailure.update { NotFound }
        }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed loading transaction $id" }
      mutableFailure.update { Failure.Other(e.requireMessage()) }
    }
  }

  private suspend fun loadEntities(): Entities {
    // Transfer payees are left out until the editor handles transfers
    val payees =
      payeeDao.getAllNonTransfer().mapNotNull { row -> row.name?.let { EntityOption(row.id, it) } }
    val categories = categoryDao.getAllActiveGrouped()
    val groups =
      categories
        .groupBy { it.groupId }
        .values
        .map { rows ->
          CategoryGroupOptions(
            name = rows.first().groupName,
            categories =
              rows
                .mapNotNull { row -> row.name?.let { EntityOption(row.id, it) } }
                .toImmutableList(),
          )
        }
    val accounts = accountDao.getAllWithStatus()
    return Entities(
      options =
        TransactionOptions(
          payees = payees.toImmutableList(),
          categoryGroups = groups.toImmutableList(),
          accounts =
            accounts
              .filter { it.closed != true }
              .mapNotNull { row -> row.name?.let { EntityOption(row.id, it) } }
              .toImmutableList(),
        ),
      payeeNames = payees.associate { it.id to it.name },
      categoryNames = groups.flatMap { it.categories }.associate { it.id to it.name },
      accountNames = accounts.mapNotNull { row -> row.name?.let { row.id to it } }.toMap(),
      offBudget = accounts.filter { it.offbudget == true }.map { it.id }.toSet(),
    )
  }

  fun startEditing() {
    val saved = mutableSaved.value ?: return
    if (saved.canEdit) mutableDraft.update { saved.fields }
  }

  // Throws away any edits, unless they're being saved
  fun stopEditing() {
    if (!mutableIsWorking.value) closeDraft()
  }

  private fun closeDraft() = mutableDraft.update { null }

  fun setAmount(amount: Amount) = updateDraft { it.copy(amount = amount) }

  fun setPayee(payee: PayeeId?) = updateDraft { it.copy(payee = payee) }

  fun setCategory(category: CategoryId?) = updateDraft { it.copy(category = category) }

  fun setAccount(account: AccountId) = updateDraft { it.copy(account = account) }

  fun setDate(date: LocalDate) = updateDraft { it.copy(date = date) }

  fun setNotes(notes: String) = updateDraft { it.copy(notes = notes) }

  fun setCleared(cleared: Boolean) = updateDraft { it.copy(cleared = cleared) }

  // As upstream's onUnlockReconciledInner, which leaves it cleared
  fun unlockReconciled() = updateDraft { it.copy(reconciled = false) }

  fun dismissError() = mutableError.update { null }

  private fun updateDraft(makeCopy: (TransactionFields) -> TransactionFields) =
    mutableDraft.update { draft ->
      if (mutableIsWorking.value) draft else draft?.let(makeCopy)
    }

  fun save() {
    val saved = mutableSaved.value ?: return
    val draft = mutableDraft.value ?: return
    if (mutableIsWorking.value) return
    val update = transactionDiff(saved.details.transaction.id, saved.fields, draft)
    if (update == null) {
      closeDraft()
      return
    }

    mutableIsWorking.update { true }
    viewModelScope.launch {
      try {
        writer.write { update(update) }
        loader.load(id)?.let { reloaded -> mutableSaved.update { reloaded.toSaved() } }
        closeDraft()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed saving $id" }
        mutableError.update { EditTransactionError.Saving(e.requireMessage()) }
      } finally {
        mutableIsWorking.update { false }
      }
    }
  }

  fun delete() {
    val saved = mutableSaved.value ?: return
    if (!saved.canEdit || mutableIsWorking.value) return

    mutableIsWorking.update { true }
    viewModelScope.launch {
      try {
        isDeleted = true
        writer.write { delete(saved.details.transaction.id) }
        mutableEvents.tryEmit(Deleted)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        isDeleted = false
        logcat.e(e) { "Failed deleting $id" }
        mutableError.update { EditTransactionError.Deleting(e.requireMessage()) }
      } finally {
        mutableIsWorking.update { false }
      }
    }
  }

  private fun LoadedTransaction.toSaved(): Saved {
    val transaction = detail.row.toTransaction(balance = Amount(balanceAfter), children = children)
    return Saved(
      details =
        TransactionDetails(
          transaction = transaction,
          cleared = detail.cleared,
          reconciled = detail.reconciled,
        ),
      fields = fields,
      canEdit = transaction.split == None && transaction.transfer == null,
    )
  }

  private data class Saved(
    val details: TransactionDetails,
    val fields: TransactionFields,
    val canEdit: Boolean,
  )

  private data class Entities(
    val options: TransactionOptions,
    val payeeNames: Map<PayeeId, String>,
    val categoryNames: Map<CategoryId, String>,
    val accountNames: Map<AccountId, String>,
    val offBudget: Set<AccountId>,
  )
}
