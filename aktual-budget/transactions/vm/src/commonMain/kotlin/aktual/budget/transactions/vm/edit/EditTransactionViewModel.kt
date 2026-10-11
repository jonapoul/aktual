package aktual.budget.transactions.vm.edit

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.rules.domain.LoadedRules
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.transactions.domain.LastTransaction
import aktual.budget.transactions.domain.LoadedTransaction
import aktual.budget.transactions.domain.NewTransaction
import aktual.budget.transactions.domain.TransactionBatch
import aktual.budget.transactions.domain.TransactionField
import aktual.budget.transactions.domain.TransactionFields
import aktual.budget.transactions.domain.TransactionLoader
import aktual.budget.transactions.domain.TransactionWriter
import aktual.budget.transactions.domain.applyRuleChanges
import aktual.budget.transactions.domain.isEmpty
import aktual.budget.transactions.domain.transactionDiff
import aktual.budget.transactions.vm.edit.EditTransactionState.Failure
import aktual.budget.transactions.vm.toTransaction
import aktual.core.Calendar
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
  @Assisted private val target: EditTransactionTarget,
  private val loader: TransactionLoader,
  private val writer: TransactionWriter,
  private val rulesLoader: TransactionRulesLoader,
  private val lastTransaction: LastTransaction,
  private val calendar: Calendar,
  private val accountDao: AccountDao,
  private val payeeDao: PayeeDao,
  private val categoryDao: CategoryDao,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted target: EditTransactionTarget): EditTransactionViewModel
  }

  // What's currently saved, following any changes made elsewhere, e.g. by a sync. Stays null for a
  // new transaction
  private val mutableSaved = MutableStateFlow<Saved?>(null)
  private val mutableEntities = MutableStateFlow<Entities?>(null)
  private val mutableFailure = MutableStateFlow<Failure?>(null)
  // The edits in progress, or null when viewing
  private val mutableDraft = MutableStateFlow<TransactionFields?>(null)
  // Payees added by name, or by a rule, which aren't created until the transaction is saved
  private val mutableNewPayees = MutableStateFlow<Map<PayeeId, String>>(emptyMap())
  private val mutableIsWorking = MutableStateFlow(false)
  private val mutableError = MutableStateFlow<EditTransactionError?>(null)
  private var isDeleted = false

  private var rules: LoadedRules? = null
  // What a new transaction started as
  private var initial: TransactionFields? = null
  // Fields of a new transaction that the user emptied, which rules mustn't fill back in. As
  // upstream's clearedFields
  private var emptied = emptySet<TransactionField>()
  private var hasUncommittedAmount = false

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
      val newPayees by mutableNewPayees.collectAsState()
      val isWorking by mutableIsWorking.collectAsState()
      buildState(saved, entities, failure, draft, newPayees, isWorking)
    }

  init {
    viewModelScope.launch { observe() }
  }

  private fun buildState(
    saved: Saved?,
    entities: Entities?,
    failure: Failure?,
    draft: TransactionFields?,
    newPayees: Map<PayeeId, String>,
    isWorking: Boolean,
  ): EditTransactionState {
    if (failure != null) return failure
    if (entities == null) return Loading
    val form = draft?.let { form(it, entities, newPayees, saved) }
    return when (target) {
      is Existing -> existingState(saved, form, isWorking)
      is New -> newState(entities, form, isWorking)
    }
  }

  private fun existingState(
    saved: Saved?,
    form: TransactionForm?,
    isWorking: Boolean,
  ): EditTransactionState {
    if (saved == null) return Loading
    val mode: TransactionEditMode = if (form == null) View else TransactionEditMode.Edit(form)
    return EditTransactionState.Loaded(
      saved = saved.details,
      mode = mode,
      canEdit = saved.canEdit,
      isWorking = isWorking,
    )
  }

  private fun newState(
    entities: Entities,
    form: TransactionForm?,
    isWorking: Boolean,
  ): EditTransactionState =
    when {
      entities.options.accounts.isEmpty() -> EditTransactionState.Empty.NoAccounts
      entities.options.categoryGroups.all { it.categories.isEmpty() } ->
        EditTransactionState.Empty.NoCategories
      form == null -> Loading
      else ->
        EditTransactionState.Loaded(
          saved = null,
          mode = TransactionEditMode.Create(form),
          canEdit = true,
          isWorking = isWorking,
        )
    }

  // A name the pickers don't offer, like a closed account's, falls back to the saved one
  private fun form(
    draft: TransactionFields,
    entities: Entities,
    newPayees: Map<PayeeId, String>,
    saved: Saved?,
  ): TransactionForm {
    val transaction = saved?.details?.transaction
    return TransactionForm(
      draft = draft,
      payeeName =
        draft.payee?.let { id ->
          entities.payeeNames[id]
            ?: newPayees[id]
            ?: transaction?.payee.takeIf { id == saved?.fields?.payee }
        },
      categoryName =
        draft.category?.let { id ->
          entities.categoryNames[id]
            ?: transaction?.category.takeIf { id == saved?.fields?.category }
        },
      accountName = draft.account?.let(entities.accountNames::get),
      isOffBudget = draft.account in entities.offBudget,
      hasChanges =
        if (saved == null) {
          draft != initial
        } else {
          transactionDiff(saved.details.transaction.id, saved.fields, draft) != null
        },
      options = entities.options,
    )
  }

  private suspend fun observe() {
    try {
      rules = rulesLoader.load()
      val entities = loadEntities()
      mutableEntities.update { entities }
      when (target) {
        is New -> startDraft(newDraft(target, entities))
        is Existing -> follow(target)
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed loading $target" }
      mutableFailure.update { Failure.Other(e.requireMessage()) }
    }
  }

  private fun startDraft(draft: TransactionFields) {
    initial = draft
    mutableDraft.update { draft }
  }

  private suspend fun follow(target: EditTransactionTarget.Existing) =
    loader.observe(target.id).collect { loaded ->
      if (loaded != null) {
        mutableFailure.update { null }
        mutableSaved.update { loaded.toSaved() }
      } else if (!isDeleted) {
        mutableFailure.update { NotFound }
      }
    }

  // As upstream's TransactionEdit.tsx, a new transaction takes its date and account from the last
  // one added, unless the list it was added from has an account of its own
  private fun newDraft(
    target: EditTransactionTarget.New,
    entities: Entities,
  ): TransactionFields {
    val last = lastTransaction.fields
    val account = target.account ?: last?.account
    return TransactionFields(
      account = account?.takeIf { id -> entities.options.accounts.any { it.id == id } },
      date = last?.date ?: calendar.today(),
      amount = Zero,
      payee = null,
      category = target.category?.takeIf { it in entities.categoryNames },
      notes = null,
      cleared = false,
      reconciled = false,
    )
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

  // Throws away any edits, unless they're being saved. There's nothing to go back to for a new
  // transaction, so that closes instead
  fun stopEditing() {
    if (mutableIsWorking.value) return
    when (target) {
      is Existing -> closeDraft()
      is New -> mutableEvents.tryEmit(Discarded)
    }
  }

  private fun closeDraft() = mutableDraft.update { null }

  // The amount changes with every key typed, so rules wait for commitAmount()
  fun setAmount(amount: Amount) {
    hasUncommittedAmount = true
    updateDraft(field = null) { it.copy(amount = amount) }
  }

  fun commitAmount() {
    if (!hasUncommittedAmount) return
    hasUncommittedAmount = false
    updateDraft(TransactionField.Amount) { it }
  }

  fun setPayee(payee: PayeeId?) = updateDraft(Payee) { it.copy(payee = payee) }

  // Picks the payee with this name, ignoring case, or a new one if there's none
  fun createPayee(name: String) {
    val rules = rules ?: return
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return
    val id = rules.resolvePayee(trimmed)
    mutableNewPayees.update { rules.createdPayees }
    setPayee(id)
  }

  fun setCategory(category: CategoryId?) = updateDraft(Category) { it.copy(category = category) }

  fun setAccount(account: AccountId) = updateDraft(Account) { it.copy(account = account) }

  fun setDate(date: LocalDate) = updateDraft(Date) { it.copy(date = date) }

  fun setNotes(notes: String) = updateDraft(Notes) { it.copy(notes = notes) }

  fun setCleared(cleared: Boolean) = updateDraft(Cleared) { it.copy(cleared = cleared) }

  // As upstream's onUnlockReconciledInner, which leaves it cleared
  fun unlockReconciled() = updateDraft(field = null) { it.copy(reconciled = false) }

  fun dismissError() = mutableError.update { null }

  // Setting a field of a new transaction runs the rules on it. Null leaves them out
  private fun updateDraft(
    field: TransactionField?,
    makeCopy: (TransactionFields) -> TransactionFields,
  ) {
    if (mutableIsWorking.value) return
    val updated = makeCopy(mutableDraft.value ?: return)
    val runsRules = field != null && target is New
    val draft = if (runsRules) withRules(updated, field) else updated
    mutableDraft.update { draft }
  }

  // As onUpdate() in upstream's TransactionEdit.tsx, which only runs rules on new transactions
  private fun withRules(draft: TransactionFields, updated: TransactionField): TransactionFields {
    val rules = rules ?: return draft
    emptied = if (draft.isEmpty(updated)) emptied + updated else emptied - updated
    val result = rules.run(draft.toRuleTransaction())
    mutableNewPayees.update { rules.createdPayees }
    return applyRuleChanges(draft, draft.afterRules(result), updated, emptied)
  }

  fun save() {
    if (mutableIsWorking.value) return
    commitAmount()
    val draft = mutableDraft.value ?: return
    when (target) {
      is Existing -> saveEdits(target, draft)
      is New -> saveNew(draft)
    }
  }

  private fun saveEdits(target: EditTransactionTarget.Existing, draft: TransactionFields) {
    val saved = mutableSaved.value ?: return
    val id = saved.details.transaction.id
    if (transactionDiff(id, saved.fields, draft) == null) {
      closeDraft()
      return
    }

    launchSave {
      writer.write {
        val fields = draft.copy(payee = savedPayee(draft.payee))
        transactionDiff(id, saved.fields, fields)?.let { update(it) }
      }
      loader.load(target.id)?.let { reloaded -> mutableSaved.update { reloaded.toSaved() } }
      closeDraft()
    }
  }

  private fun saveNew(draft: TransactionFields) {
    val account = draft.account ?: return
    launchSave {
      writer.write {
        val transaction =
          NewTransaction(
            account = account,
            date = draft.date,
            amount = draft.amount,
            payee = savedPayee(draft.payee),
            category = draft.category,
            notes = draft.notes?.ifEmpty { null },
            cleared = draft.cleared,
          )
        insert(transaction)
      }
      lastTransaction.set(draft)
      mutableEvents.tryEmit(Created)
    }
  }

  private fun launchSave(save: suspend () -> Unit) {
    mutableIsWorking.update { true }
    viewModelScope.launch {
      try {
        save()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed saving $target" }
        mutableError.update { EditTransactionError.Saving(e.requireMessage()) }
      } finally {
        mutableIsWorking.update { false }
      }
    }
  }

  // Creates the payee if it's one of the new ones, unless one with its name has turned up since
  private suspend fun TransactionBatch.savedPayee(id: PayeeId?): PayeeId? {
    val name = id?.let(mutableNewPayees.value::get) ?: return id
    return createPayee(name)
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
        logcat.e(e) { "Failed deleting $target" }
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

  private fun TransactionFields.toRuleTransaction() =
    RuleTransaction(
      account = account ?: NO_ACCOUNT,
      date = date,
      amount = amount,
      payee = payee,
      category = category,
      notes = notes,
      cleared = cleared,
      reconciled = reconciled,
    )

  private fun TransactionFields.afterRules(result: RuleTransaction) =
    copy(
      account = result.account.takeIf { it != NO_ACCOUNT },
      date = result.date,
      amount = result.amount,
      // TODO(#1761): a rule that splits the transaction moves its payee to the parts, which the
      // editor doesn't have yet
      payee = if (result.isParent) payee else result.payee,
      category = result.category,
      notes = result.notes,
      cleared = result.cleared,
    )

  private companion object {
    // Upstream runs rules on a new transaction before it has an account, which RuleTransaction
    // needs
    val NO_ACCOUNT = AccountId("")
  }
}
