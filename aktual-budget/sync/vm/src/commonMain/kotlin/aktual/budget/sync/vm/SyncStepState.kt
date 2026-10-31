package aktual.budget.sync.vm

import aktual.core.model.Percent
import androidx.compose.runtime.Immutable

@Immutable
sealed interface SyncStepState {
  data object NotStarted : SyncStepState

  sealed interface InProgress : SyncStepState

  data object IndefiniteProgress : InProgress

  data class DefiniteProgress(val progress: Percent) : InProgress

  sealed interface Stopped : SyncStepState

  data class Failed(val moreInfo: String) : Stopped

  data object Succeeded : Stopped
}
