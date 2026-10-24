package aktual.budget.banksync.domain

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.ExternalBankAccount
import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.BankSyncDao
import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.BANKS
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.BankId
import aktual.budget.model.LocalChange
import aktual.budget.model.localChange
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.UuidGenerator
import aktual.di.BudgetCoroutineScope
import dev.zacsweers.metro.Inject
import kotlin.coroutines.cancellation.CancellationException
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
  private val writer: TransactionWriter,
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
    val account = writer.write {
      insertAccount(external.name, offBudget).also { insertPayee(name = "", transferAccount = it) }
    }
    val changes = mutableListOf<LocalChange>()
    changes.addLink(account, source, external)
    syncController.syncChanges(changes)
    scope.launch { bankSync.sync(setOf(account)) }
    return account
  }

  /**
   * Clears [account]'s link. Once nothing else uses its GoCardless bank login, that's deleted too.
   * Failing to delete it doesn't stop the unlink, as upstream, but returns false.
   */
  suspend fun unlink(account: AccountId): Boolean {
    val row = checkNotNull(accountDao[account]) { "Account $account not found" }
    val bank = row.bank ?: return true
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
    syncController.syncChanges(columns.map { LocalChange(ACCOUNTS, account.value, it, Null) })

    if (row.account_sync_source != GoCardless || dao.bankUsers(bank) > 0) return true
    val requisition = dao.bankId(bank) ?: return true
    return try {
      api.removeGoCardlessRequisition(requisition.value).also { removed ->
        if (!removed) logcat.w { "GoCardless refused to delete requisition $requisition" }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed deleting GoCardless requisition $requisition" }
      false
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
          bankId?.let { this += localChange(BANKS, id, "bank_id", it.value) }
          this += localChange(BANKS, id, "name", external.institution)
        }
    this += localChange(ACCOUNTS, account.value, "account_id", external.accountId)
    this += localChange(ACCOUNTS, account.value, "bank", bank)
    this += localChange(ACCOUNTS, account.value, "account_sync_source", source.value)
  }
}
