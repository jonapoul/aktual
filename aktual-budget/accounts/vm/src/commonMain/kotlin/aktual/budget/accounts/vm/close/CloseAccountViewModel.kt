package aktual.budget.accounts.vm.close

import aktual.budget.accounts.domain.AccountWriter
import aktual.budget.banksync.domain.AccountDeleter
import aktual.budget.banksync.domain.BankAccountLinker
import aktual.budget.db.GetManageList
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import aktual.budget.model.CategoryId
import aktual.core.utils.runCatchingCancellable
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The close sheet and delete dialog, as upstream's CloseAccountModal. Closing follows
 * closeAccount(), then unlinks bank sync. Deleting is its forced branch, see [AccountDeleter].
 */
@Stable
@AssistedInject
class CloseAccountViewModel
internal constructor(
  @Assisted private val accountId: AccountId,
  private val writer: AccountWriter,
  private val linker: BankAccountLinker,
  private val deleter: AccountDeleter,
  categoryDao: CategoryDao,
  accountDao: AccountDao,
  transactionDao: TransactionDao,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val transferTo = MutableStateFlow<AccountId?>(null)
  private val category = MutableStateFlow<CategoryId?>(null)
  private val isWorking = MutableStateFlow(false)
  private val workError = MutableStateFlow<String?>(null)

  private val mutableEvents =
    MutableSharedFlow<CloseAccountEvent>(extraBufferCapacity = 1, onBufferOverflow = DROP_OLDEST)
  val events: SharedFlow<CloseAccountEvent> = mutableEvents.asSharedFlow()

  // Loaded once: a transfer or category added while the sheet's open isn't worth a reload
  private val extras = flow {
    val transfers = transactionDao.accountTransfers(accountId)
    val count = transfers.values.count { linked -> linked != null && linked !in transfers }
    emit(Extras(count, categoryDao.getAllGrouped().toCategoryGroups()))
  }

  // Closing an empty account or deleting one drops it from the list. Keeps the last state until
  // the sheet closes, rather than failing
  private val source =
    combine(accountDao.observeManageList(), extras) { rows, extras ->
        rows
          .firstOrNull { it.id == accountId }
          ?.let { account ->
            val targets =
              rows
                .filter { it.id != accountId && it.closed != true }
                .map { TransferTarget(it.id, it.name.orEmpty(), it.offbudget == true) }
            Source(account, extras, targets.toImmutableList())
          }
      }
      .scan<Source?, Source?>(null) { last, next -> next ?: last ?: error("No account $accountId") }
      .filterNotNull()

  private val choices =
    combine(transferTo, category, isWorking, workError) { transferTo, category, working, error ->
      Choices(transferTo, category, working, error)
    }

  val state: StateFlow<CloseAccountState> =
    combine<Source, Choices, CloseAccountState>(source, choices, ::ready)
      .catch { e ->
        log.e(e) { "Failed loading account $accountId" }
        emit(Failure(e.requireMessage()))
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun setTransferTo(id: AccountId?) = transferTo.update { id }

  fun setCategory(id: CategoryId?) = category.update { id }

  fun dismissError() = workError.update { null }

  fun close() {
    val ready = state.value as? Ready ?: return
    if (!ready.canClose) return
    val target = ready.transferTo.takeIf { ready.case == HasBalance }
    val category = ready.category.takeIf { ready.needsCategory }
    launchWork("closing") {
      val outcome = writer.close(accountId, target, category)
      val unlinked = !ready.isLinked || unlink()
      val event =
        CloseAccountEvent.Closed(
          name = ready.name,
          deleted = outcome.result == Deleted,
          unlinkFailed = !unlinked,
          undo = outcome.undo,
        )
      mutableEvents.tryEmit(event)
    }
  }

  fun delete() {
    val ready = state.value as? Ready ?: return
    if (ready.isWorking) return
    launchWork("deleting") {
      val unlinked = deleter.delete(accountId)
      mutableEvents.tryEmit(CloseAccountEvent.Deleted(ready.name, unlinkFailed = !unlinked))
    }
  }

  // Stays working once done, so it can't run twice before the sheet closes
  private fun launchWork(description: String, work: suspend () -> Unit) {
    isWorking.update { true }
    workError.update { null }
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = { work() },
        onFailure = { e ->
          log.e(e) { "Failed $description account $accountId" }
          workError.update { e.requireMessage() }
          isWorking.update { false }
        },
      )
    }
  }

  private suspend fun unlink(): Boolean =
    runCatchingCancellable(
      attempt = { linker.unlink(accountId) },
      onFailure = { e ->
        log.w(e) { "Closed $accountId but failed unlinking it" }
        false
      },
    )

  private fun ready(source: Source, choices: Choices): Ready {
    val account = source.account
    val balance = Amount(account.balance)
    val case: CloseCase =
      when {
        account.transaction_count == 0L -> NoTransactions
        balance == Zero -> ZeroBalance
        else -> HasBalance
      }
    val target = source.targets.firstOrNull { it.id == choices.transferTo }
    val needsCategory = case == HasBalance && account.offbudget != true && target?.offBudget == true
    val hasTarget = case != HasBalance || target != null
    val categories = source.extras.categories
    val category = categories.firstNotNullOfOrNull { group ->
      group.categories.firstOrNull { it.id == choices.category }
    }
    return Ready(
      name = account.name.orEmpty(),
      balance = balance,
      transactionCount = account.transaction_count,
      transferCount = source.extras.transferCount,
      isLinked = account.account_sync_source != null,
      case = case,
      targets = source.targets,
      transferTo = target?.id,
      needsCategory = needsCategory,
      categories = categories,
      category = category?.id,
      categoryName = category?.name,
      canClose = !choices.isWorking && hasTarget && (!needsCategory || category != null),
      isWorking = choices.isWorking,
      error = choices.error,
    )
  }

  private data class Source(
    val account: GetManageList,
    val extras: Extras,
    val targets: ImmutableList<TransferTarget>,
  )

  private data class Choices(
    val transferTo: AccountId?,
    val category: CategoryId?,
    val isWorking: Boolean,
    val error: String?,
  )

  private data class Extras(
    val transferCount: Int,
    val categories: ImmutableList<CategoryChoiceGroup>,
  )

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(accountId: AccountId): CloseAccountViewModel
  }
}
