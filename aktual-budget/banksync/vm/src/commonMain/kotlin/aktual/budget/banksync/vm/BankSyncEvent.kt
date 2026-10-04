package aktual.budget.banksync.vm

import aktual.budget.banksync.domain.BankSyncSummary
import androidx.compose.runtime.Immutable

@Immutable
sealed interface BankSyncEvent {
  /** A bank sync finished, whichever screen started it. */
  @JvmInline value class Finished(val summary: BankSyncSummary) : BankSyncEvent
}
