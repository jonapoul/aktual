package aktual.budget.banksync.vm

import aktual.budget.banksync.domain.BankSyncSummary

sealed interface BankSyncEvent {
  @JvmInline value class Finished(val summary: BankSyncSummary) : BankSyncEvent
}
