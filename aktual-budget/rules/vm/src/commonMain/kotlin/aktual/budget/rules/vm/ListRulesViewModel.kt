package aktual.budget.rules.vm

import aktual.budget.db.Rules
import aktual.budget.db.dao.RulesDao
import aktual.budget.di.BudgetGraphHolder
import aktual.budget.model.ConditionOp
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.rules.vm.CheckboxesState.Active
import aktual.budget.rules.vm.CheckboxesState.Inactive
import aktual.budget.rules.vm.ListRulesState.Empty
import aktual.budget.rules.vm.ListRulesState.Failure
import aktual.budget.rules.vm.ListRulesState.Loading
import aktual.budget.rules.vm.ListRulesState.Success
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ListRulesViewModel(budgetGraphs: BudgetGraphHolder) : ViewModel() {
  private val database = budgetGraphs.require().database
  private val rulesDao = RulesDao(database)

  val nameFetcher: NameFetcher = NameFetcherImpl(database)

  private val mutableRules = MutableStateFlow<ImmutableList<RuleListItem>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private val mutableCheckboxes = MutableStateFlow<CheckboxesState>(Inactive)

  val checkboxes: StateFlow<CheckboxesState> = mutableCheckboxes.asStateFlow()

  val state: StateFlow<ListRulesState> =
    viewModelScope.launchMolecule(Immediate) {
      val rules by mutableRules.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      when {
        isLoading -> Loading
        failure != null -> Failure(failure)
        rules.isEmpty() -> Empty
        else -> Success(rules)
      }
    }

  init {
    reload()
  }

  fun reload() {
    mutableIsLoading.update { true }
    viewModelScope.launch {
      try {
        val rules = rulesDao.getAll().map(::toListItem).toImmutableList()
        mutableRules.update { rules }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading all rules" }
        mutableFailure.update { e.requireMessage() }
      } finally {
        mutableIsLoading.update { false }
      }
    }
  }

  fun delete(id: RuleId) = delete(persistentSetOf(id))

  fun delete(ids: ImmutableSet<RuleId>) {
    viewModelScope.launch {
      val numDeleted = rulesDao.delete(ids)
      mutableCheckboxes.update { Inactive }
      reload()
      logcat.d { "Deleted $numDeleted rules: $ids" }
    }
  }

  fun showCheckboxes() = mutableCheckboxes.update { Active(persistentSetOf()) }

  fun hideCheckboxes() = mutableCheckboxes.update { Inactive }

  fun check(id: RuleId) = mutableCheckboxes.update { it + id }

  fun uncheck(id: RuleId) = mutableCheckboxes.update { it - id }

  fun uncheckAll() = showCheckboxes()

  private fun toListItem(rule: Rules): RuleListItem =
    RuleListItem(
      id = rule.id,
      stage = rule.stage ?: RuleStage.Default,
      conditions = rule.conditions.orEmpty().toImmutableList(),
      conditionsOp = rule.conditions_op ?: ConditionOp.And,
      actions = rule.actions.orEmpty().toImmutableList(),
    )
}
