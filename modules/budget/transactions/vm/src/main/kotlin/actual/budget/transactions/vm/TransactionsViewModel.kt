package actual.budget.transactions.vm

import actual.account.model.LoginToken
import actual.budget.db.dao.AccountsDao
import actual.budget.di.BudgetComponentStateHolder
import actual.budget.model.BudgetId
import actual.budget.model.TransactionId
import actual.budget.model.TransactionsSpec
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

@Suppress("unused")
@HiltViewModel(assistedFactory = TransactionsViewModel.Factory::class)
class TransactionsViewModel @AssistedInject constructor(
  @Assisted inputs: Inputs,
  components: BudgetComponentStateHolder,
) : ViewModel() {
  private val accountsDao = components
    .map { it?.database?.let(::AccountsDao) }
    .stateIn(viewModelScope, Eagerly, initialValue = null)

  val transactions: StateFlow<ImmutableList<DatedTransactions>> = MutableStateFlow(persistentListOf())
  val title: StateFlow<String> = MutableStateFlow("")
  val format: StateFlow<TransactionsFormat> = MutableStateFlow(TransactionsFormat.List)
  val sorting: StateFlow<SortBy> = MutableStateFlow(SortBy.Date(ascending = false))

  fun isChecked(id: TransactionId): Flow<Boolean> = flowOf()
  fun isExpanded(date: LocalDate): Flow<Boolean> = flowOf()

  fun setChecked(id: TransactionId, isChecked: Boolean) = Unit
  fun setExpanded(date: LocalDate, isExpanded: Boolean) = Unit

  data class Inputs(
    val token: LoginToken,
    val budgetId: BudgetId,
    val spec: TransactionsSpec,
  )

  @AssistedFactory
  fun interface Factory {
    fun create(
      @Assisted inputs: Inputs,
    ): TransactionsViewModel
  }
}
