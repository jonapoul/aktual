package aktual.budget.banksync.vm

import aktual.budget.banksync.domain.BankSyncController
import aktual.budget.banksync.domain.BankSyncProgress
import aktual.budget.banksync.domain.BankSyncResult
import aktual.budget.model.AccountId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// Records what it's asked to sync, and reports whatever progress the test gives it
internal class FakeBankSyncController : BankSyncController {
  override val progress = MutableStateFlow(BankSyncProgress())
  override val finished = MutableSharedFlow<List<BankSyncResult>>(extraBufferCapacity = 1)
  val started = mutableListOf<Set<AccountId>>()

  override fun start(accounts: Set<AccountId>): Boolean {
    if (progress.value.isRunning) return false
    started += accounts
    return true
  }

  override suspend fun sync(accounts: Set<AccountId>): List<BankSyncResult> = error("Not used")

  fun running(pending: List<AccountId>) = progress.update {
    BankSyncProgress(isRunning = true, pending = pending)
  }

  suspend fun finish(vararg results: BankSyncResult) {
    progress.update { BankSyncProgress(results = results.toList()) }
    finished.emit(results.toList())
  }
}
