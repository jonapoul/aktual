package aktual.prefs.vm.advanced

import aktual.budget.model.DbMetadataKey.Companion.GroupId
import aktual.budget.model.DbMetadataKey.Companion.Id
import aktual.budget.transactions.domain.RepairResult
import aktual.budget.transactions.domain.TransactionRepairer
import aktual.budget.utils.BudgetLocalPreferences
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class AdvancedSettingsViewModel
internal constructor(
  private val repairer: TransactionRepairer,
  localPreferences: BudgetLocalPreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val repair = MutableStateFlow<RepairState>(Idle)

  val state: StateFlow<AdvancedSettingsState> =
    combine(localPreferences, repair) { metadata, repair ->
        AdvancedSettingsState(budgetId = metadata[Id], syncId = metadata[GroupId], repair = repair)
      }
      .stateIn(
        viewModelScope,
        Eagerly,
        AdvancedSettingsState(
          budgetId = localPreferences[Id],
          syncId = localPreferences[GroupId],
          repair = Idle,
        ),
      )

  fun repairTransactions() {
    if (repair.value == Running) return
    repair.update { Running }
    viewModelScope.launch {
      val result =
        runCatchingCancellable(
          attempt = { repairer.repair().toState() },
          onFailure = { e ->
            log.e(e) { "Failed repairing transactions" }
            RepairState.Failed
          },
        )
      repair.update { result }
    }
  }

  private fun RepairResult.toState() =
    RepairState.Done(
      blankPayees = blankPayees,
      cleared = cleared,
      deleted = deleted,
      transfers = transfers,
      nonParentErrors = nonParentErrors,
      categorisedParents = categorisedParents,
      mismatchedSplits = mismatchedSplits.toImmutableList(),
    )
}
