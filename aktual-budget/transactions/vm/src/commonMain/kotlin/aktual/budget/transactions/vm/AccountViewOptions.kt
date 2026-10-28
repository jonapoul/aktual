package aktual.budget.transactions.vm

import androidx.compose.runtime.Immutable

// One account's list options, synced with upstream
@Immutable
data class AccountViewOptions(
  val showRunningBalance: Boolean,
  val hideReconciled: Boolean,
  val showExtraBalances: Boolean,
)

enum class AccountOption {
  RunningBalance,
  HideReconciledRows,
  ExtraBalances,
}
