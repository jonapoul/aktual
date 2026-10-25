package aktual.budget.payees.vm.list

import aktual.budget.db.GetPayeeList
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.PayeeId
import aktual.budget.rules.domain.PayeeRuleCounter
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListPayeesViewModel(
  private val payeeDao: PayeeDao,
  private val ruleCounter: PayeeRuleCounter,
) : ViewModel() {
  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  // Reloads whenever the payees, their transactions or the rules change, so synced and local edits
  // show up without a manual refresh
  val state: StateFlow<ListPayeesState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        combine(payeeDao.observeListChanges(), ruleCounter.observeChanges()) { _, _ -> }
          .mapLatest { load() }
          .onStart { emit(Loading) }
          .catch { e ->
            logcat.e(e) { "Failed loading payees" }
            emit(Failure(e.requireMessage()))
          }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = Loading)

  fun retry() {
    retries.tryEmit(Unit)
  }

  private suspend fun load(): ListPayeesState {
    val ruleCounts = ruleCounter.count()
    val payees = payeeDao.list().map { it.toPayeeItem(ruleCounts) }
    return if (payees.isEmpty()) Empty else Success(payees.toImmutableList())
  }

  private fun GetPayeeList.toPayeeItem(ruleCounts: Map<PayeeId, Int>) =
    PayeeItem(
      id = id,
      name = name.orEmpty(),
      isTransfer = transfer_acct != null,
      isFavorite = favorite == true,
      numTransactions = num_transactions.toInt(),
      numRules = ruleCounts[id] ?: 0,
    )
}
