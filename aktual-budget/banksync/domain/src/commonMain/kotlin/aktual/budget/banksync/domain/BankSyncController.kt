package aktual.budget.banksync.domain

import aktual.api.client.BankSyncApi
import aktual.api.model.banksync.BankSyncTransactionsRequest
import aktual.api.model.banksync.BankSyncTransactionsResponse
import aktual.api.model.banksync.BankSyncTransactionsResponse.ProviderError
import aktual.api.model.banksync.BankSyncTransactionsResponse.Rejected
import aktual.api.model.banksync.BankSyncTransactionsResponse.Success
import aktual.api.model.banksync.SimpleFinBatchRequest
import aktual.api.model.banksync.SimpleFinBatchResponse
import aktual.budget.db.dao.BankSyncAccount
import aktual.budget.db.dao.BankSyncDao
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import aktual.budget.transactions.domain.AccountUpdate
import aktual.budget.transactions.domain.Patch
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.Calendar
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import logcat.logcat

/**
 * Downloads transactions for the open budget's linked accounts and imports them: accountsBankSync()
 * and simpleFinBatchSync() in packages/loot-core/src/server/accounts/app.ts. Each account is synced
 * in turn, recording when it last synced or why it failed, and the changes go through
 * [TransactionWriter] like any others.
 *
 * Several SimpleFIN accounts are downloaded in one request, as the desktop client does when syncing
 * everything, since SimpleFIN limits how often it's asked for data.
 */
@Inject
@SingleIn(BudgetScope::class)
class BankSyncController(
  private val api: BankSyncApi,
  private val importer: BankSyncImporter,
  private val writer: TransactionWriter,
  private val dao: BankSyncDao,
  private val calendar: Calendar,
  private val clock: Clock,
  private val scope: BudgetCoroutineScope,
) {
  private val mutableProgress = MutableStateFlow(BankSyncProgress())
  val progress: StateFlow<BankSyncProgress> = mutableProgress.asStateFlow()

  private val mutex = Mutex()

  /**
   * Starts syncing [accounts], or every linked account if empty, in the budget's scope so that it
   * carries on after the screen that started it closes. Returns false if a sync is already running.
   */
  fun start(accounts: Set<AccountId> = emptySet()): Boolean {
    if (!mutex.tryLock()) return false
    scope.launch {
      try {
        run(accounts)
      } finally {
        mutex.unlock()
      }
    }
    return true
  }

  /** As [start], but waits for any running sync to finish first, then for this one. */
  suspend fun sync(accounts: Set<AccountId> = emptySet()): List<BankSyncResult> = mutex.withLock {
    run(accounts)
  }

  private suspend fun run(ids: Set<AccountId>): List<BankSyncResult> {
    val accounts = dao.accounts().filter { ids.isEmpty() || it.id in ids }
    val pending = accounts.map(BankSyncAccount::id)
    mutableProgress.update { BankSyncProgress(isRunning = true, pending = pending) }
    try {
      val simpleFin = accounts.filter { it.source == AccountSyncSource.SimpleFin }
      val batch = if (simpleFin.size > 1) simpleFin else emptyList()
      if (batch.isNotEmpty()) syncSimpleFin(batch)
      for (account in accounts - batch.toSet()) {
        report(syncAccount(account))
      }
    } finally {
      mutableProgress.update { it.copy(isRunning = false, pending = emptyList()) }
    }
    return mutableProgress.value.results
  }

  private fun report(result: BankSyncResult) {
    mutableProgress.update {
      it.copy(pending = it.pending - result.account, results = it.results + result)
    }
  }

  // syncAccount() in packages/loot-core/src/server/accounts/sync.ts
  private suspend fun syncAccount(account: BankSyncAccount): BankSyncResult {
    val source =
      account.source ?: return fail(account, BankSyncError.Internal("No bank sync provider"))
    val oldest = dao.oldestDate(account.id, calendar.today())
    val initialSync = oldest == null
    val request =
      BankSyncTransactionsRequest(
        accountId = account.accountId,
        startDate = startDate(oldest),
        requisitionId = account.bankId.value.takeIf { source == AccountSyncSource.GoCardless },
        includeBalance = initialSync.takeIf { source == AccountSyncSource.GoCardless },
        aspspName = account.bankName.takeIf { source == AccountSyncSource.EnableBanking },
      )
    val response =
      try {
        api.transactions(source, request)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.w(e) { "Failed downloading ${account.id}" }
        return fail(account, BankSyncError.Internal(e.message))
      }
    return handle(account, response, initialSync)
  }

  // simpleFinBatchSync() in packages/loot-core/src/server/accounts/sync.ts
  private suspend fun syncSimpleFin(accounts: List<BankSyncAccount>) {
    val today = calendar.today()
    val oldest = accounts.associate { it.id to dao.oldestDate(it.id, today) }
    val request =
      SimpleFinBatchRequest(
        accountIds = accounts.map { it.accountId },
        startDates = accounts.map { startDate(oldest[it.id]) },
      )
    val response =
      try {
        api.simpleFinBatch(request)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.w(e) { "Failed downloading SimpleFIN accounts" }
        for (account in accounts) report(fail(account, BankSyncError.Internal(e.message)))
        return
      }

    for (account in accounts) {
      val result =
        when (response) {
          is SimpleFinBatchResponse.Failed -> {
            fail(account, response.error.toError())
          }
          is SimpleFinBatchResponse.Success -> {
            val download =
              response.accounts[account.accountId]
                ?: ProviderError(ProviderError.ACCOUNT_MISSING, ProviderError.ACCOUNT_MISSING)
            handle(account, download, initialSync = oldest[account.id] == null)
          }
        }
      report(result)
    }
  }

  private suspend fun handle(
    account: BankSyncAccount,
    response: BankSyncTransactionsResponse,
    initialSync: Boolean,
  ): BankSyncResult =
    when (response) {
      is Success -> import(account, response, initialSync)
      is BankSyncTransactionsResponse.Failure -> fail(account, response.toError())
    }

  // processBankSyncDownload() then handleSyncResponse()
  private suspend fun import(
    account: BankSyncAccount,
    response: Success,
    initialSync: Boolean,
  ): BankSyncResult {
    val download =
      BankSyncDownload(
        transactions = response.transactions.all,
        currentBalance = response.startingBalance?.let(::Amount),
      )
    val result =
      try {
        importer.import(account.id, account.source, download, initialSync)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.w(e) { "Failed importing ${account.id}" }
        return fail(account, BankSyncError.Internal(e.message))
      }
    updateAccount(account.id, BankSyncStatus.Ok, synced = true)
    return BankSyncResult.Synced(account.id, account.name, result.added, result.updated)
  }

  private suspend fun fail(account: BankSyncAccount, error: BankSyncError): BankSyncResult {
    logcat.w { "Bank sync failed for ${account.id}: $error" }
    updateAccount(account.id, error.status, synced = false)
    return BankSyncResult.Failed(account.id, account.name, error)
  }

  private suspend fun updateAccount(id: AccountId, status: BankSyncStatus, synced: Boolean) {
    writer.write {
      updateAccount(
        AccountUpdate(
          id = id,
          lastSync = if (synced) Patch.To(clock.now()) else Patch.Keep,
          bankSyncStatus = Patch.To(status),
        )
      )
    }
  }

  // getAccountSyncStartDate(). Many GoCardless banks only give 90 days of transactions, so it's
  // never further back than that, today included
  private fun startDate(oldestTransaction: LocalDate?): LocalDate {
    val earliest = calendar.today().minus(SYNC_DAYS - 1, DateTimeUnit.DAY)
    return if (oldestTransaction == null) earliest else maxOf(earliest, oldestTransaction)
  }

  private fun BankSyncTransactionsResponse.Failure.toError(): BankSyncError =
    when (this) {
      is ProviderError -> BankSyncError.Provider(errorType, errorCode, reason)
      is Rejected -> BankSyncError.Rejected(reason)
    }

  private companion object {
    const val SYNC_DAYS = 90
  }
}
