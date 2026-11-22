package aktual.budget.list.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ImportBudgetState {
  data object Idle : ImportBudgetState

  data object Importing : ImportBudgetState

  @JvmInline value class Failed(val reason: ImportFailure) : ImportBudgetState
}

enum class ImportFailure {
  NotZip,
  MissingFiles,
  BadMetadata,
  Unknown,
}
