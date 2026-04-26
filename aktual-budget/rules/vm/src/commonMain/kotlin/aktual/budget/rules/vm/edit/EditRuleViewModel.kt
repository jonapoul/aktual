package aktual.budget.rules.vm.edit

import aktual.budget.db.DbJson
import aktual.budget.db.Rules
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.LocalChange
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.tombstone
import aktual.budget.di.BudgetGraphHolder
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.ConditionType
import aktual.budget.model.Field
import aktual.budget.model.MessageValue
import aktual.budget.model.Operator
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.model.messageValue
import aktual.budget.rules.vm.EntityListFetcher
import aktual.budget.rules.vm.EntityListFetcherImpl
import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.NameFetcherImpl
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.edit.EditRuleState.Failure
import aktual.core.model.UuidGenerator
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import logcat.logcat

@Stable
@AssistedInject
class EditRuleViewModel(
  @Assisted private val ruleId: RuleId?,
  private val uuidGenerator: UuidGenerator,
  budgetGraphs: BudgetGraphHolder,
) : ViewModel() {
  private val budgetGraph = budgetGraphs.require()
  private val rulesDao = RulesDao(budgetGraph.database)

  private val mutableEvents =
    MutableSharedFlow<EditRuleEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )

  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableIsWorking = MutableStateFlow(false)
  private val mutableFailure = MutableStateFlow<Failure?>(null)
  private val mutableRule = MutableStateFlow<Rule?>(null)

  val nameFetcher: NameFetcher = NameFetcherImpl(budgetGraph.database)

  val entityListFetcher: EntityListFetcher = EntityListFetcherImpl(budgetGraph.database)

  val events: SharedFlow<EditRuleEvent> = mutableEvents.asSharedFlow()

  val state: StateFlow<EditRuleState> =
    viewModelScope.launchMolecule(Immediate) {
      val isLoading by mutableIsLoading.collectAsState()
      val isWorking by mutableIsWorking.collectAsState()
      val failure by mutableFailure.collectAsState()
      val rule by mutableRule.collectAsState()
      editRuleState(isLoading, isWorking, failure, rule)
    }

  init {
    loadData()
  }

  private fun loadData() {
    mutableIsLoading.update { true }

    viewModelScope.launch {
      if (ruleId == null) {
        mutableRule.update { emptyRule() }
        mutableIsLoading.update { false }
        return@launch
      }

      try {
        val rule = rulesDao[ruleId]
        if (rule == null) {
          mutableFailure.update { Failure.NoMatch }
        } else {
          val model =
            Rule(
              id = rule.id,
              stage = rule.stage ?: RuleStage.Default,
              conditions = rule.conditions.orEmpty().toImmutableList(),
              conditionsOp = rule.conditions_op ?: ConditionOp.Default,
              actions = rule.actions.orEmpty().toImmutableList(),
            )
          mutableRule.update { model }
        }
      } catch (e: Exception) {
        logcat.e(e) { "Failed loading $ruleId" }
        mutableFailure.update { Failure.Other(e.requireMessage()) }
      } finally {
        mutableIsLoading.update { false }
      }
    }
  }

  fun delete() {
    val id = ruleId ?: return

    mutableIsWorking.update { true }
    viewModelScope.launch {
      try {
        rulesDao.tombstone(setOf(id))
        mutableEvents.tryEmit(EditRuleEvent.DeletedRule)
        val change = tombstone(dataset = RULES, row = id.toString())
        budgetGraph.syncController.syncChanges(change)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed tombstoning $id" }
      } finally {
        mutableIsWorking.update { false }
      }
    }
  }

  fun save() {
    mutableIsWorking.update { true }

    viewModelScope.launch {
      try {
        val model = mutableRule.value
        if (model == null) {
          logcat.w { "Tried to save without a rule?" }
        } else {
          val rule =
            with(model) {
              Rules(
                id = id,
                stage = stage,
                conditions = conditions,
                actions = actions,
                tombstone = false,
                conditions_op = conditionsOp,
              )
            }
          rulesDao.insert(rule)
          logcat.i { "Saved $rule" }
          budgetGraph.syncController.syncChanges(insertChanges(rule))
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed saving $ruleId" }
        mutableFailure.update { Failure.Saving(e.requireMessage()) }
      } finally {
        mutableIsWorking.update { false }
      }
    }
  }

  fun setStage(stage: RuleStage) = updateRule { r -> r.copy(stage = stage) }

  fun setConditionOp(op: ConditionOp) = updateRule { r -> r.copy(conditionsOp = op) }

  fun setConditionField(field: Field, index: Int) =
    updateCondition(index) { c -> c.copy(field = field, value = JsonNull) }

  fun setConditionOperator(operator: Operator, index: Int) =
    updateCondition(index) { c -> c.copy(operator = operator) }

  fun setConditionValue(value: JsonElement, index: Int) =
    updateCondition(index) { c -> c.copy(value = value) }

  fun deleteCondition(index: Int) = updateRule { r ->
    val conditions = r.conditions.toPersistentList().removeAt(index)
    r.copy(conditions = conditions)
  }

  fun appendCondition() {
    val condition = emptyCondition()
    updateRule { rule -> rule.copy(conditions = (rule.conditions + condition).toImmutableList()) }
  }

  private fun updateRule(makeCopy: (Rule) -> Rule) = mutableRule.update { rule ->
    rule ?: return@update null
    makeCopy(rule)
  }

  private fun updateCondition(index: Int, makeCopy: (Condition) -> Condition) = updateRule { rule ->
    val conditions = rule.conditions.toMutableList()
    val condition = makeCopy(conditions[index])
    conditions[index] = condition
    rule.copy(conditions = conditions.toImmutableList())
  }

  private fun emptyRule() =
    Rule(
      id = RuleId(uuidGenerator()),
      stage = RuleStage.Default,
      conditionsOp = ConditionOp.Default,
      conditions = persistentListOf(emptyCondition()),
      actions = persistentListOf(emptyAction()),
    )

  private fun emptyCondition() =
    Condition(field = Field.Payee, operator = Is, type = ConditionType.Id, value = JsonNull)

  private fun emptyAction() =
    RuleAction(
      value = null,
      op = RuleAction.Op.Default,
      field = Field.Payee,
      type = RuleAction.Type.Default,
    )

  private fun insertChanges(rule: Rules): List<LocalChange> {
    fun change(column: String, value: MessageValue) =
      LocalChange(RULES, rule.id.toString(), column, value)

    return with(rule) {
      listOf(
        change("id", id.toString().messageValue()),
        change("stage", stage?.value.messageValue()),
        change("conditions", DbJson.encodeToString(conditions.orEmpty()).messageValue()),
        change("actions", DbJson.encodeToString(actions.orEmpty()).messageValue()),
        change("tombstone", tombstone.messageValue()),
        change("conditions_op", conditions_op?.value.messageValue()),
      )
    }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(ruleId: RuleId?): EditRuleViewModel
  }
}
