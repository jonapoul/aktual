package aktual.budget.rules.vm.list

import aktual.budget.BudgetSyncController
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.model.RuleId
import aktual.budget.model.tombstone
import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.RulesLoader
import aktual.budget.rules.vm.list.ListRulesState.Empty
import aktual.budget.rules.vm.list.ListRulesState.Failure
import aktual.budget.rules.vm.list.ListRulesState.Loading
import aktual.budget.rules.vm.list.ListRulesState.Success
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListRulesViewModel(
  loader: RulesLoader,
  private val syncController: BudgetSyncController,
  val nameFetcher: NameFetcher,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val state: StateFlow<ListRulesState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        loader
          .observe()
          .map { rules -> if (rules.isEmpty()) Empty else Success(rules) }
          .onStart<ListRulesState> { emit(Loading) }
          .catch { e ->
            logcat.e(e) { "Failed loading all rules" }
            emit(Failure(e.requireMessage()))
          }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun retry() {
    retries.tryEmit(Unit)
  }

  fun delete(id: RuleId) {
    viewModelScope.launch {
      syncController.syncChanges(listOf(tombstone(dataset = RULES, row = id.toString())))
      logcat.d { "Tombstoned rule $id" }
    }
  }
}
