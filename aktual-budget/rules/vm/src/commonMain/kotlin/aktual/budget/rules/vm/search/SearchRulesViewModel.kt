package aktual.budget.rules.vm.search

import aktual.budget.model.PayeeId
import aktual.budget.rules.domain.NameFetcher
import aktual.budget.rules.domain.Rule
import aktual.budget.rules.domain.RulesLoader
import aktual.budget.rules.domain.TransactionRule
import aktual.budget.rules.domain.payeeIds
import aktual.budget.rules.vm.RuleSearchText
import aktual.budget.rules.vm.search.SearchRulesState.Failure
import aktual.budget.rules.vm.search.SearchRulesState.Results
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@Stable
@AssistedInject
class SearchRulesViewModel
internal constructor(
  @Assisted initialPayee: PayeeId?,
  @Assisted private val savedState: SavedStateHandle,
  private val loader: RulesLoader,
  private val searchText: RuleSearchText,
  val nameFetcher: NameFetcher,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted initialPayee: PayeeId?,
      @Assisted savedState: SavedStateHandle,
    ): SearchRulesViewModel
  }

  init {
    // Only the first time, so clearing the filter survives process death
    if (KEY_PAYEE !in savedState) savedState[KEY_PAYEE] = initialPayee?.value
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  private val payee: StateFlow<PayeeId?> =
    savedState
      .getStateFlow<String?>(KEY_PAYEE, initialValue = null)
      .map { it?.let(::PayeeId) }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  // The name of the payee the results are filtered to, or null when there's no filter
  val payeeName: StateFlow<String?> =
    payee
      .flatMapLatest { id ->
        if (id == null) flowOf(null) else nameFetcher.name(Payee, id.value).map { it.orEmpty() }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val state: StateFlow<SearchRulesState> =
    retries
      .onStart { emit(Unit) }
      .flatMapLatest {
        combine(query, payee, searchableRules(), ::search).catch { e ->
          log.e(e) { "Failed loading rules" }
          emit(Failure(e.requireMessage()))
        }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  fun reload() {
    retries.tryEmit(Unit)
  }

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  fun clearPayee() {
    savedState[KEY_PAYEE] = null
  }

  // Null until the first load finishes
  private fun searchableRules(): Flow<List<SearchableRule>?> =
    loader
      .observe()
      .mapLatest<_, List<SearchableRule>?> { rules ->
        rules.map { SearchableRule(it, searchText.of(it), it.toTransactionRule().payeeIds()) }
      }
      .onStart { emit(null) }

  private fun search(
    query: String,
    payee: PayeeId?,
    rules: List<SearchableRule>?,
  ): SearchRulesState {
    val trimmed = query.trim()
    if (trimmed.isEmpty() && payee == null) return NoQuery
    if (rules == null) return Loading

    val matching =
      rules
        .filter {
          (payee == null || payee in it.payees) && it.text.contains(trimmed, ignoreCase = true)
        }
        .map { it.rule }
    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching.toImmutableList())
    }
  }

  private fun Rule.toTransactionRule() =
    TransactionRule(id = id, conditions = conditions, actions = actions)

  private data class SearchableRule(val rule: Rule, val text: String, val payees: Set<PayeeId>)

  private companion object {
    const val KEY_QUERY = "query"
    const val KEY_PAYEE = "payee"
  }
}
