package dev.jonpoulton.actual.listbudgets.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface BudgetFile {
  val budget: Budget
  val state: BudgetState
}

@Immutable
data class LocalFile(
  override val budget: LocalBudget,
) : BudgetFile {
  override val state = BudgetState.Local
}

@Immutable
data class SyncableLocalFile(
  override val budget: RemoteBudget,
  override val state: BudgetState, // broken or unknown
) : BudgetFile

@Immutable
data class SyncedLocalFile(
  override val budget: RemoteBudget,
  override val state: BudgetState, // synced or detached
  val hasKey: Boolean,
  val encryptKeyId: String?,
) : BudgetFile

@Immutable
data class RemoteFile(
  override val budget: RemoteBudget,
  val hasKey: Boolean,
  val encryptKeyId: String?,
) : BudgetFile {
  override val state = BudgetState.Remote
}
