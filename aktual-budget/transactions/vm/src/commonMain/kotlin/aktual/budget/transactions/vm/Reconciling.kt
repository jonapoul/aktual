package aktual.budget.transactions.vm

import aktual.budget.model.Amount
import androidx.compose.runtime.Immutable
import kotlin.time.Instant

// An account being reconciled against the bank's balance, as upstream's ReconcilingBanner shows it
@Immutable
data class Reconciling(val target: Amount, val cleared: Amount, val isBusy: Boolean = false) {
  // What the cleared balance needs to match the bank's
  val difference: Amount
    get() = target - cleared
}

// What the reconcile dialog starts from, as upstream's AccountReconcileModal
@Immutable
data class ReconcileStart(
  val cleared: Amount,
  // The balance the bank last reported, for a linked account
  val lastSynced: Amount?,
  val lastReconciled: Instant?,
)
