package aktual.budget.banksync.domain

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.ExternalBankAccount
import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.BankSyncDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.BANKS
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.BankId
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.messageValue
import aktual.core.UuidGenerator
import aktual.di.BudgetCoroutineScope
import dev.zacsweers.metro.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.floor
import kotlinx.coroutines.launch
import logcat.logcat

/**
 * Links budget accounts to accounts at a bank, or unlinks them. See linkSimpleFinAccount() and
 * unlinkAccount() in packages/loot-core/src/server/accounts/app.ts.
 */
@Inject
class BankAccountLinker(
  private val api: BankSyncApi,
  private val accountDao: AccountDao,
  private val dao: BankSyncDao,
  private val syncController: BudgetSyncController,
  private val bankSync: BankSyncController,
  private val uuidGenerator: UuidGenerator,
  private val scope: BudgetCoroutineScope,
) {
  /**
   * Links [account] to [external], which [source] listed, then starts its first sync in the
   * budget's scope, after any sync already running.
   */
  suspend fun link(account: AccountId, source: AccountSyncSource, external: ExternalBankAccount) {
    checkNotNull(accountDao[account]) { "Account $account not found" }
    val changes = mutableListOf<LocalChange>()
    changes.addLink(account, source, external)
    syncController.syncChanges(changes)
    scope.launch { bankSync.sync(setOf(account)) }
  }

  /**
   * Adds a budget account named after [external], which [source] listed, at the end of the on or
   * [offBudget] list with its transfer payee, then syncs it as [link] does. Its first sync adds the
   * starting balance. See linkGoCardlessAccount() without an upgradingId.
   */
  suspend fun create(
    source: AccountSyncSource,
    external: ExternalBankAccount,
    offBudget: Boolean,
  ): AccountId {
    val account = AccountId(uuidGenerator())
    // Messages only carry whole numbers, so rounding down still puts it after a fractional order
    val sortOrder =
      (accountDao.maxSortOrder(offBudget)?.let(::floor)?.toLong() ?: 0L) + SORT_INCREMENT
    val changes =
      mutableListOf(
        change(account, "name", external.name.messageValue()),
        change(account, "offbudget", offBudget.messageValue()),
        change(account, "sort_order", MessageValue.Number(sortOrder)),
      )
    changes.addLink(account, source, external)
    val payee = uuidGenerator()
    changes += LocalChange(PAYEES, payee, "name", "".messageValue())
    changes += LocalChange(PAYEES, payee, "transfer_acct", account.value.messageValue())
    syncController.syncChanges(changes)
    scope.launch { bankSync.sync(setOf(account)) }
    return account
  }

  /**
   * Clears [account]'s link. Once nothing else uses its GoCardless bank login, that's deleted too,
   * though failing to only gets logged, as upstream.
   */
  suspend fun unlink(account: AccountId) {
    val row = checkNotNull(accountDao[account]) { "Account $account not found" }
    val bank = row.bank ?: return
    val columns =
      listOf(
        "account_id",
        "bank",
        "balance_current",
        "balance_available",
        "balance_limit",
        "account_sync_source",
        "bank_sync_status",
      )
    syncController.syncChanges(columns.map { change(account, it, Null) })

    if (row.account_sync_source != AccountSyncSource.GoCardless || dao.bankUsers(bank) > 0) return
    val requisition = dao.bankId(bank) ?: return
    try {
      if (!api.removeGoCardlessRequisition(requisition.value)) {
        logcat.w { "GoCardless refused to delete requisition $requisition" }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed deleting GoCardless requisition $requisition" }
    }
  }

  // Links [account] to [external], adding its bank first unless there already is one
  private suspend fun MutableList<LocalChange>.addLink(
    account: AccountId,
    source: AccountSyncSource,
    external: ExternalBankAccount,
  ) {
    val bankId = (external.orgDomain ?: external.orgId)?.let(::BankId)
    val bank =
      dao.findBank(bankId, external.institution)?.toString()
        ?: uuidGenerator().also { id ->
          bankId?.let { this += LocalChange(BANKS, id, "bank_id", it.value.messageValue()) }
          this += LocalChange(BANKS, id, "name", external.institution.messageValue())
        }
    this += change(account, "account_id", external.accountId.messageValue())
    this += change(account, "bank", bank.messageValue())
    this += change(account, "account_sync_source", source.value.messageValue())
  }

  private fun change(account: AccountId, column: String, value: MessageValue) =
    LocalChange(ACCOUNTS, account.value, column, value)
}

// packages/loot-core/src/shared/util.ts
private const val SORT_INCREMENT = 16384L
