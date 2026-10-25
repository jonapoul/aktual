package aktual.budget.payees.vm.list

import aktual.budget.db.GetPayeeList
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.PayeeId
import aktual.budget.rules.domain.PayeeRuleCounter
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListPayeesViewModel(
  private val payeeDao: PayeeDao,
  private val ruleCounter: PayeeRuleCounter,
) : ViewModel() {
  private val mutablePayees = MutableStateFlow<ImmutableList<PayeeItem>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableIsRefreshing = MutableStateFlow(false)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private var reloadJob: Job? = null

  val isRefreshing: StateFlow<Boolean> = mutableIsRefreshing.asStateFlow()

  val state: StateFlow<ListPayeesState> =
    viewModelScope.launchMolecule(Immediate) {
      val payees by mutablePayees.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        payees.isEmpty() -> Empty
        else -> Success(payees)
      }
    }

  init {
    reload()
  }

  fun reload(showLoading: Boolean = true) = load(loading = showLoading, refreshing = false)

  fun refresh() = load(loading = false, refreshing = true)

  private fun load(loading: Boolean, refreshing: Boolean) {
    mutableIsLoading.update { loading }
    mutableIsRefreshing.update { refreshing }
    reloadJob?.cancel()
    reloadJob = viewModelScope.launch {
      try {
        val ruleCounts = ruleCounter.count()
        val payees = payeeDao.list().map { it.toPayeeItem(ruleCounts) }.toImmutableList()
        mutablePayees.update { payees }
        mutableFailure.update { null }
      } catch (e: CancellationException) {
        // a newer load() cancelled us - leave the flows alone so it can finish
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading payees" }
        mutableFailure.update { e.requireMessage() }
      } finally {
        mutableIsLoading.update { false }
        mutableIsRefreshing.update { false }
      }
    }
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
