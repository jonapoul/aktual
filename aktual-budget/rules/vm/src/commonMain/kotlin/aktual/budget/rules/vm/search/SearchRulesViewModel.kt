package aktual.budget.rules.vm.search

import aktual.budget.model.PayeeId
import aktual.budget.rules.domain.TransactionRule
import aktual.budget.rules.domain.payeeIds
import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.RuleSearchText
import aktual.budget.rules.vm.RulesLoader
import aktual.budget.rules.vm.search.SearchRulesState.Failure
import aktual.budget.rules.vm.search.SearchRulesState.Results
import aktual.core.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

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

  private val rules = MutableStateFlow<List<SearchableRule>?>(null)
  private val failure = MutableStateFlow<String?>(null)

  val state: StateFlow<SearchRulesState> =
    combine(query, payee, rules, failure, ::search)
      .stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  init {
    reload()
  }

  fun reload() {
    failure.update { null }
    viewModelScope.launch {
      runCatchingCancellable {
        val loaded =
          loader.load().map { rule ->
            SearchableRule(rule, searchText.of(rule), rule.toTransactionRule().payeeIds())
          }
        rules.update { loaded }
      }
        .onFailure { e ->
          logcat.e(e) { "Failed loading rules" }
          failure.update { e.requireMessage() }
        }
    }
  }

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  fun clearPayee() {
    savedState[KEY_PAYEE] = null
  }

  private fun search(
    query: String,
    payee: PayeeId?,
    rules: List<SearchableRule>?,
    failure: String?,
  ): SearchRulesState {
    if (failure != null) return Failure(failure)
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
