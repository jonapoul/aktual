package aktual.budget.rules.vm

import aktual.budget.db.Rules
import aktual.budget.db.dao.RulesDao
import aktual.budget.di.BudgetGraphHolder
import aktual.budget.model.BudgetId
import aktual.budget.model.ConditionOp
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.core.model.Token
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
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

@Suppress("unused") // TODO
@Stable
@AssistedInject
class ListRulesViewModel(
  @Assisted private val token: Token,
  @Assisted private val budgetId: BudgetId,
  budgetGraphs: BudgetGraphHolder,
) : ViewModel() {
  private val rulesDao = RulesDao(budgetGraphs.require().database)

  private val mutableRules = MutableStateFlow<ImmutableList<RuleListItem>>(persistentListOf())
  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableFailure = MutableStateFlow<String?>(null)
  private val mutableCheckboxes = MutableStateFlow<CheckboxesState>(CheckboxesState.Inactive)

  val checkboxes: StateFlow<CheckboxesState> = mutableCheckboxes.asStateFlow()

  val state: StateFlow<ListRulesState> =
    viewModelScope.launchMolecule(Immediate) {
      val rules by mutableRules.collectAsState()
      val isLoading by mutableIsLoading.collectAsState()
      val failure by mutableFailure.collectAsState()
      when {
        isLoading -> ListRulesState.Loading
        failure != null -> ListRulesState.Failure(failure)
        rules.isEmpty() -> ListRulesState.Empty
        else -> ListRulesState.Success(rules)
      }
    }

  init {
    reload()
  }

  fun reload() {
    mutableIsLoading.update { true }
    mutableRules.update { persistentListOf() }
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

  fun delete(id: RuleId) {
    // TBC
  }

  fun showCheckboxes() = mutableCheckboxes.update { CheckboxesState.Active(persistentSetOf()) }

  fun hideCheckboxes() = mutableCheckboxes.update { CheckboxesState.Inactive }

  fun check(id: RuleId) = mutableCheckboxes.update { it + id }

  fun uncheck(id: RuleId) = mutableCheckboxes.update { it - id }

  private fun toListItem(rule: Rules): RuleListItem =
    RuleListItem(
      id = rule.id,
      stage = rule.stage ?: RuleStage.Default,
      conditions = rule.conditions.orEmpty().toImmutableList(),
      conditionsOp = rule.conditions_op ?: ConditionOp.And,
      actions = rule.actions.orEmpty().toImmutableList(),
    )

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted token: Token, @Assisted budgetId: BudgetId): ListRulesViewModel
  }
}
