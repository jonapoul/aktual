package actual.budget.sync.vm

import actual.core.model.Percent
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
sealed interface SyncStepState {
  data object NotStarted : SyncStepState

  sealed interface InProgress : SyncStepState {
    data object Indefinite : InProgress
    @Poko class Definite(val progress: Percent) : InProgress
  }

  sealed interface Stopped : SyncStepState
  @Poko class Failed(val moreInfo: String) : Stopped
  data object Succeeded : Stopped
}
