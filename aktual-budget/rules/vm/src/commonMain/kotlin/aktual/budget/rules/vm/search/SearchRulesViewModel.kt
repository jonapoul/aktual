package aktual.budget.rules.vm.search

import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.RuleSearchText
import aktual.budget.rules.vm.RulesLoader
import aktual.budget.rules.vm.search.SearchRulesState.Failure
import aktual.budget.rules.vm.search.SearchRulesState.Results
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class SearchRulesViewModel
internal constructor(
  @Assisted private val savedState: SavedStateHandle,
  private val loader: RulesLoader,
  private val searchText: RuleSearchText,
  val nameFetcher: NameFetcher,
) : ViewModel() {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SearchRulesViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SearchRulesViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): SearchRulesViewModel
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  private val rules = MutableStateFlow<List<SearchableRule>?>(null)
  private val failure = MutableStateFlow<String?>(null)

  val state: StateFlow<SearchRulesState> =
    combine(query, rules, failure, ::search)
      .stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  init {
    reload()
  }

  fun reload() {
    failure.update { null }
    viewModelScope.launch {
      try {
        val loaded = loader.load().map { SearchableRule(it, searchText.of(it)) }
        rules.update { loaded }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading rules" }
        failure.update { e.requireMessage() }
      }
    }
  }

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  private fun search(
    query: String,
    rules: List<SearchableRule>?,
    failure: String?,
  ): SearchRulesState {
    if (failure != null) return Failure(failure)
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return NoQuery
    if (rules == null) return Loading

    val matching = rules.filter { it.text.contains(trimmed, ignoreCase = true) }.map { it.rule }
    return if (matching.isEmpty()) {
      NoResults
    } else {
      Results(trimmed, matching.toImmutableList())
    }
  }

  private data class SearchableRule(val rule: Rule, val text: String)

  private companion object {
    const val KEY_QUERY = "query"
  }
}
