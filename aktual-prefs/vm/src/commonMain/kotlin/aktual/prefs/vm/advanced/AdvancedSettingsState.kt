package aktual.prefs.vm.advanced

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

// syncId is null for a budget that isn't on a server
@Immutable
data class AdvancedSettingsState(
  val budgetId: String?,
  val syncId: String?,
  val repair: RepairState,
)

@Immutable
sealed interface RepairState {
  data object Idle : RepairState

  data object Running : RepairState

  data object Failed : RepairState

  data class Done(
    val blankPayees: Int,
    val cleared: Int,
    val deleted: Int,
    val transfers: Int,
    val nonParentErrors: Int,
    val categorisedParents: Int,
    val mismatchedSplits: ImmutableList<LocalDate>,
  ) : RepairState {
    val isEmpty: Boolean
      get() =
        blankPayees == 0 &&
          cleared == 0 &&
          deleted == 0 &&
          transfers == 0 &&
          nonParentErrors == 0 &&
          categorisedParents == 0 &&
          mismatchedSplits.isEmpty()
  }
}
