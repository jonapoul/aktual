package aktual.budget.payees.vm.list

import aktual.budget.payees.vm.PayeesLoader
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListPayeesViewModel internal constructor(loader: PayeesLoader) : ViewModel() {
  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val state: StateFlow<ListPayeesState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        loader
          .observe()
          .map { payees -> if (payees.isEmpty()) Empty else Success(payees.toImmutableList()) }
          .onStart<ListPayeesState> { emit(Loading) }
          .catch { e ->
            logcat.e(e) { "Failed loading payees" }
            emit(Failure(e.requireMessage()))
          }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun retry() {
    retries.tryEmit(Unit)
  }
}
