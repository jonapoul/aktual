package aktual.budget.rules.vm.list

import aktual.budget.db.Rules
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.tombstone
import aktual.budget.di.BudgetGraphHolder
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Operator
import aktual.budget.model.Operator.Contains
import aktual.budget.model.Operator.DoesNotContain
import aktual.budget.model.Operator.GreaterThan
import aktual.budget.model.Operator.GreaterThanOrEquals
import aktual.budget.model.Operator.HasTags
import aktual.budget.model.Operator.Is
import aktual.budget.model.Operator.IsApprox
import aktual.budget.model.Operator.IsBetween
import aktual.budget.model.Operator.IsNot
import aktual.budget.model.Operator.LessThan
import aktual.budget.model.Operator.LessThanOrEquals
import aktual.budget.model.Operator.Matches
import aktual.budget.model.Operator.NotOneOf
import aktual.budget.model.Operator.OffBudget
import aktual.budget.model.Operator.OnBudget
import aktual.budget.model.Operator.OneOf
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.list.CheckboxesState.Inactive
import aktual.budget.rules.vm.list.ListRulesState.Empty
import aktual.budget.rules.vm.list.ListRulesState.Loading
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
  private val budgetGraph = budgetGraphs.require()
  private val rulesDao = RulesDao(budgetGraph.database)

  val nameFetcher: NameFetcher = NameFetcherImpl(budgetGraph.database)

  private val mutableRules = MutableStateFlow<ImmutableList<Rule>>(persistentListOf())
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
        failure != null -> ListRulesState.Failure(failure)
        rules.isEmpty() -> Empty
        else -> ListRulesState.Success(rules)
      }
    }

  init {
    reload()
  }

  fun reload() {
    mutableIsLoading.update { true }
    viewModelScope.launch {
      try {
        val rules = rulesDao.getAll().map(::toRule).sortedWith(RuleComparator).toImmutableList()
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
      val changes = ids.map { id -> tombstone(dataset = RULES, row = id.toString()) }
      budgetGraph.syncController.syncChanges(changes)
      mutableCheckboxes.update { Inactive }
      reload()
      logcat.d { "Tombstoned ${ids.size} rules: $ids" }
    }
  }

  fun showCheckboxes() = mutableCheckboxes.update { CheckboxesState.Active(persistentSetOf()) }

  fun hideCheckboxes() = mutableCheckboxes.update { Inactive }

  fun check(id: RuleId) = mutableCheckboxes.update { it + id }

  fun uncheck(id: RuleId) = mutableCheckboxes.update { it - id }

  fun uncheckAll() = showCheckboxes()

  private fun toRule(rule: Rules): Rule =
    Rule(
      id = rule.id,
      stage = rule.stage ?: RuleStage.Default,
      conditions = rule.conditions.orEmpty().toImmutableList(),
      conditionsOp = rule.conditions_op ?: ConditionOp.And,
      actions = rule.actions.orEmpty().toImmutableList(),
    )

  /**
   * Sorts rules to match execution order. Mirrors rankRules in
   * packages/loot-core/src/server/rules/rule-utils.ts. Groups by stage (pre -> default -> post),
   * then sorts within each stage by condition specificity score, with rule ID as tiebreaker.
   */
  private object RuleComparator : Comparator<Rule> {
    private val EXACT_OPERATORS = setOf(Is, IsNot, IsApprox, OneOf, NotOneOf)

    override fun compare(a: Rule, b: Rule): Int {
      val stageDiff = a.stage.ordinal - b.stage.ordinal
      if (stageDiff != 0) return stageDiff
      val scoreDiff = computeScore(b.conditions) - computeScore(a.conditions)
      if (scoreDiff != 0) return scoreDiff
      return a.id.compareTo(b.id)
    }

    private fun computeScore(conditions: List<Condition>): Int {
      val score = conditions.sumOf { operatorScore(it.operator) }
      val allExact = conditions.isNotEmpty() && conditions.all { it.operator in EXACT_OPERATORS }
      return if (allExact) score * 2 else score
    }

    @Suppress("MagicNumber")
    private fun operatorScore(op: Operator): Int =
      when (op) {
        Is,
        IsNot -> 10
        OneOf,
        NotOneOf -> 9
        IsApprox,
        IsBetween -> 5
        GreaterThan,
        GreaterThanOrEquals,
        LessThan,
        LessThanOrEquals -> 1
        Contains,
        DoesNotContain,
        Matches,
        HasTags,
        OnBudget,
        OffBudget -> 0
      }
  }
}
