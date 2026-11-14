package aktual.budget.rules.vm.edit

import aktual.budget.db.DbJson
import aktual.budget.db.Rules
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.LocalChange
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.rules.domain.ConditionEntitiesLoader
import aktual.budget.rules.domain.addAction
import aktual.budget.rules.domain.addSplit
import aktual.budget.rules.domain.newAction
import aktual.budget.rules.domain.newActionIndex
import aktual.budget.rules.domain.removeSplit
import aktual.budget.rules.domain.splitIndex
import aktual.budget.rules.vm.NameFetcher
import aktual.budget.rules.vm.Rule
import aktual.budget.rules.vm.edit.EditRuleState.Failure
import aktual.budget.rules.vm.hasUnknownValues
import aktual.budget.utils.BudgetSyncController
import aktual.core.utils.UuidGenerator
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

@Stable
@AssistedInject
class EditRuleViewModel
internal constructor(
  @Assisted private val ruleId: RuleId?,
  @Assisted private val payee: PayeeId?,
  private val uuidGenerator: UuidGenerator,
  private val rulesDao: RulesDao,
  private val syncController: BudgetSyncController,
  val nameFetcher: NameFetcher,
  conditionEntitiesLoader: ConditionEntitiesLoader,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableEvents =
    MutableSharedFlow<EditRuleEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )

  private val mutableIsLoading = MutableStateFlow(true)
  private val mutableIsWorking = MutableStateFlow(false)
  private val mutableFailure = MutableStateFlow<Failure?>(null)
  private val mutableDraft = MutableStateFlow<Draft?>(null)
  private var nextKey = 0L

  val events: SharedFlow<EditRuleEvent> = mutableEvents.asSharedFlow()

  val conditionEntities: StateFlow<ConditionEntities?> =
    conditionEntitiesLoader
      .observe()
      .stateIn(viewModelScope, WhileSubscribed(STOP_TIMEOUT_MS), initialValue = null)

  val state: StateFlow<EditRuleState> =
    viewModelScope.launchMolecule(Immediate) {
      val isLoading by mutableIsLoading.collectAsState()
      val isWorking by mutableIsWorking.collectAsState()
      val failure by mutableFailure.collectAsState()
      val draft by mutableDraft.collectAsState()
      editRuleState(isLoading, isWorking, failure, draft)
    }

  init {
    loadData()
  }

  private fun loadData() {
    mutableIsLoading.update { true }

    viewModelScope.launch {
      if (ruleId == null) {
        mutableDraft.update { draft(if (payee == null) emptyRule() else payeeRule(payee)) }
        mutableIsLoading.update { false }
        return@launch
      }

      try {
        val rule = rulesDao[ruleId]
        if (rule == null) {
          mutableFailure.update { NotFound }
        } else {
          val model =
            Rule(
              id = rule.id,
              stage = rule.stage ?: Default,
              conditions = rule.conditions.orEmpty().toImmutableList(),
              conditionsOp = rule.conditions_op ?: Default,
              actions = rule.actions.orEmpty().toImmutableList(),
            )
          mutableDraft.update { draft(model) }
        }
      } catch (e: Exception) {
        log.e(e) { "Failed loading $ruleId" }
        mutableFailure.update { EditRuleState.OtherFailure(e.requireMessage()) }
      } finally {
        mutableIsLoading.update { false }
      }
    }
  }

  fun delete() {
    val id = ruleId ?: return

    mutableIsWorking.update { true }
    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          rulesDao.tombstone(setOf(id))
          mutableEvents.tryEmit(DeletedRule)
          val change = tombstone(dataset = RULES, row = id.toString())
          syncController.syncChanges(change)
        },
        onFailure = { e -> log.e(e) { "Failed tombstoning $id" } },
        finally = { mutableIsWorking.update { false } },
      )
    }
  }

  fun save() {
    mutableIsWorking.update { true }

    viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val model = mutableDraft.value?.rule
          if (model == null) {
            log.w { "Tried to save without a rule?" }
          } else if (model.hasUnknownValues) {
            log.w { "Not saving $model, it has unknown values" }
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
            log.i { "Saved $rule" }
            syncController.syncChanges(insertChanges(rule))
          }
        },
        onFailure = { e ->
          log.e(e) { "Failed saving $ruleId" }
          mutableFailure.update { EditRuleState.SaveFailed(e.requireMessage()) }
        },
        finally = { mutableIsWorking.update { false } },
      )
    }
  }

  fun setStage(stage: RuleStage) = updateRule { r -> r.copy(stage = stage) }

  fun setConditionsOp(op: ConditionOp) = updateRule { r -> r.copy(conditionsOp = op) }

  fun setCondition(index: Int, condition: Condition) = updateRule { r ->
    r.copy(conditions = r.conditions.toPersistentList().replacingAt(index, condition))
  }

  fun addCondition() {
    val key = nextKey++
    mutableDraft.update { draft ->
      draft?.copy(
        rule =
          draft.rule.copy(
            conditions = draft.rule.conditions.toPersistentList().adding(emptyCondition()),
          ),
        conditionKeys = draft.conditionKeys.adding(key),
      )
    }
  }

  fun removeCondition(index: Int) = mutableDraft.update { draft ->
    draft?.copy(
      rule =
        draft.rule.copy(conditions = draft.rule.conditions.toPersistentList().removingAt(index)),
      conditionKeys = draft.conditionKeys.removingAt(index),
    )
  }

  fun setAction(index: Int, action: RuleAction) = updateRule { r ->
    r.copy(actions = r.actions.toPersistentList().replacingAt(index, action))
  }

  fun addAction(splitIndex: Int = 0) {
    val key = nextKey++
    mutableDraft.update { draft ->
      val actions = draft?.rule?.actions ?: return@update draft
      draft.copy(
        rule = draft.rule.copy(actions = actions.addAction(splitIndex).toImmutableList()),
        actionKeys = draft.actionKeys.addingAt(actions.newActionIndex(splitIndex), key),
      )
    }
  }

  fun removeAction(index: Int) = mutableDraft.update { draft ->
    draft?.copy(
      rule = draft.rule.copy(actions = draft.rule.actions.toPersistentList().removingAt(index)),
      actionKeys = draft.actionKeys.removingAt(index),
    )
  }

  fun addSplit() = mutableDraft.update { draft ->
    val actions = draft?.rule?.actions ?: return@update draft
    val added = actions.addSplit()
    draft.copy(
      rule = draft.rule.copy(actions = added.toImmutableList()),
      actionKeys = draft.actionKeys.addingAll(List(added.size - actions.size) { nextKey++ }),
    )
  }

  fun removeSplit(splitIndex: Int) = mutableDraft.update { draft ->
    if (draft == null || splitIndex == 0) return@update draft
    val actions = draft.rule.actions
    draft.copy(
      rule = draft.rule.copy(actions = actions.removeSplit(splitIndex).toImmutableList()),
      actionKeys =
        draft.actionKeys
          .filterIndexed { i, _ -> actions[i].splitIndex != splitIndex }
          .toPersistentList(),
    )
  }

  private fun updateRule(makeCopy: (Rule) -> Rule) = mutableDraft.update { draft ->
    draft?.copy(rule = makeCopy(draft.rule))
  }

  private fun draft(rule: Rule) =
    Draft(
      rule = rule,
      conditionKeys = rule.conditions.map { nextKey++ }.toPersistentList(),
      actionKeys = rule.actions.map { nextKey++ }.toPersistentList(),
    )

  private fun emptyRule() =
    Rule(
      id = uuidGenerator(::RuleId),
      stage = Default,
      conditionsOp = Default,
      conditions = persistentListOf(emptyCondition()),
      actions = persistentListOf(newAction(Category)),
    )

  // Matches upstream's "Create rule" from the payees screen
  private fun payeeRule(payee: PayeeId) =
    emptyRule()
      .copy(
        conditions =
          persistentListOf(
            Condition(field = Payee, operator = Is, type = Id, value = JsonPrimitive(payee.value)),
          ),
      )

  private fun emptyCondition() =
    Condition(field = Payee, operator = Is, type = Id, value = JsonNull)

  private fun insertChanges(rule: Rules): List<LocalChange> {
    val row = rule.id.toString()
    return with(rule) {
      listOf(
        localChange(RULES, row, "id", row),
        localChange(RULES, row, "stage", stage),
        localChange(
          RULES,
          row,
          "conditions",
          DbJson.encodeToString(conditions.orEmpty()),
        ),
        localChange(RULES, row, "actions", DbJson.encodeToString(actions.orEmpty())),
        localChange(RULES, row, "tombstone", tombstone),
        localChange(RULES, row, "conditions_op", conditions_op),
      )
    }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(ruleId: RuleId?, payee: PayeeId?): EditRuleViewModel
  }
}

private const val STOP_TIMEOUT_MS = 5_000L

// Keys stay with their condition or action as others are added or removed, so the UI can animate
// them
internal data class Draft(
  val rule: Rule,
  val conditionKeys: PersistentList<Long>,
  val actionKeys: PersistentList<Long>,
)
