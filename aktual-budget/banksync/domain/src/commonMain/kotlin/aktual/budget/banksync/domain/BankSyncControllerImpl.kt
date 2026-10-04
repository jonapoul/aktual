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
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import aktual.budget.transactions.domain.AccountUpdate
import aktual.budget.transactions.domain.Patch
import aktual.budget.transactions.domain.TransactionWriter
import aktual.core.Calendar
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import logcat.logcat

@Inject
@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class, binding<BankSyncController>())
class BankSyncControllerImpl(
  private val api: BankSyncApi,
  private val importer: BankSyncImporter,
  private val writer: TransactionWriter,
  private val dao: BankSyncDao,
  private val calendar: Calendar,
  private val clock: Clock,
  private val scope: BudgetCoroutineScope,
) : BankSyncController {
  private val mutableProgress = MutableStateFlow(BankSyncProgress())
  override val progress: StateFlow<BankSyncProgress> = mutableProgress.asStateFlow()

  private val mutableFinished = MutableSharedFlow<List<BankSyncResult>>(extraBufferCapacity = 1)

  override val finished: SharedFlow<List<BankSyncResult>> = mutableFinished.asSharedFlow()

  private val mutex = Mutex()

  override fun start(accounts: Set<AccountId>): Boolean {
    if (!mutex.tryLock()) return false
    scope.launch {
      try {
        run(accounts)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed bank sync" }
      } finally {
        mutex.unlock()
      }
    }
    return true
  }

  override suspend fun sync(accounts: Set<AccountId>): List<BankSyncResult> = mutex.withLock {
    run(accounts)
  }

  private suspend fun run(ids: Set<AccountId>): List<BankSyncResult> {
    val accounts = dao.accounts().filter { ids.isEmpty() || it.id in ids }
    val pending = accounts.map(BankSyncAccount::id)
    mutableProgress.update { BankSyncProgress(isRunning = true, pending = pending) }
    try {
      val simpleFin = accounts.filter { it.source == SimpleFin }
      val batch = if (simpleFin.size > 1) simpleFin else emptyList()
      if (batch.isNotEmpty()) syncSimpleFin(batch)
      for (account in accounts - batch.toSet()) {
        report(syncAccount(account))
      }
    } finally {
      mutableProgress.update { it.copy(isRunning = false, pending = emptyList()) }
    }
    val results = mutableProgress.value.results
    mutableFinished.emit(results)
    return results
  }

  private fun report(result: BankSyncResult) {
    mutableProgress.update {
      it.copy(pending = it.pending - result.account, results = it.results + result)
    }
  }

  // syncAccount() in packages/loot-core/src/server/accounts/sync.ts
  private suspend fun syncAccount(account: BankSyncAccount): BankSyncResult =
    catching(account) {
      val source =
        account.source
          ?: return@catching fail(account, BankSyncError.Internal("No bank sync provider"))
      val oldest = dao.oldestDate(account.id, calendar.today())
      val initialSync = oldest == null
      val request =
        BankSyncTransactionsRequest(
          accountId = account.accountId,
          startDate = startDate(oldest),
          requisitionId = account.bankId.value.takeIf { source == GoCardless },
          includeBalance = initialSync.takeIf { source == GoCardless },
          aspspName = account.bankName.takeIf { source == EnableBanking },
        )
      handle(account, api.transactions(source, request), initialSync)
    }

  // Fails the account with whatever's thrown, so it can't stop the other accounts syncing
  private suspend inline fun catching(
    account: BankSyncAccount,
    block: () -> BankSyncResult,
  ): BankSyncResult =
    try {
      block()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed syncing ${account.id}" }
      fail(account, BankSyncError.Internal(e.message))
    }

  // simpleFinBatchSync() in packages/loot-core/src/server/accounts/sync.ts
  private suspend fun syncSimpleFin(accounts: List<BankSyncAccount>) {
    val (oldest, response) =
      try {
        val today = calendar.today()
        val oldest = accounts.associate { it.id to dao.oldestDate(it.id, today) }
        val request =
          SimpleFinBatchRequest(
            accountIds = accounts.map { it.accountId },
            startDates = accounts.map { startDate(oldest[it.id]) },
          )
        oldest to api.simpleFinBatch(request)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.w(e) { "Failed downloading SimpleFIN accounts" }
        for (account in accounts) report(fail(account, BankSyncError.Internal(e.message)))
        return
      }

    for (account in accounts) {
      val result =
        catching(account) {
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
    val result = importer.import(account.id, account.source, download, initialSync)
    updateAccount(account.id, Ok, synced = true)
    return BankSyncResult.Synced(account.id, account.name, result.added, result.updated)
  }

  private suspend fun fail(account: BankSyncAccount, error: BankSyncError): BankSyncResult {
    logcat.w { "Bank sync failed for ${account.id}: $error" }
    try {
      updateAccount(account.id, error.status, synced = false)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed recording the status of ${account.id}" }
    }
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
    val earliest = calendar.today().minus(SYNC_DAYS - 1, DAY)
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
