package aktual.budget.rules.domain

import aktual.budget.db.DbJson
import aktual.budget.db.RuleJson
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.ConditionType
import aktual.budget.model.LocalChange
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.Op.Set
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.model.localChange
import aktual.budget.model.tombstone
import aktual.budget.utils.BudgetSync
import aktual.core.utils.UuidGenerator
import aktual.di.BudgetScope
import aktual.log
import dev.zacsweers.metro.ContributesBinding
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@ContributesBinding(BudgetScope::class)
class PayeeRenameRulesImpl
internal constructor(
  private val rulesDao: RulesDao,
  private val budgetSync: BudgetSync,
  private val uuidGenerator: UuidGenerator,
) : PayeeRenameRules {
  override suspend fun add(fromNames: Collection<String>, to: PayeeId): PayeeRename? {
    val names = fromNames.filter { it.isNotEmpty() }
    if (names.isEmpty()) return null
    val existing = rulesDao.getAll().map { it.toTransactionRule() }.firstOrNull { it.renamesTo(to) }
    val rename = if (existing == null) create(names, to) else extend(existing, names)
    if (rename != null) log.i { "Added ${names.size} payee renames to $to" }
    return rename
  }

  override suspend fun undo(rename: PayeeRename) =
    when (rename) {
      is Created -> budgetSync(tombstone(RULES, rename.rule.value))
      is Extended -> budgetSync(conditionsChange(rename.rule, rename.oldCondition))
    }

  private suspend fun create(names: List<String>, to: PayeeId): PayeeRename {
    val id = uuidGenerator(::RuleId)
    budgetSync(newRule(id.value, names, to))
    return PayeeRename.Created(id)
  }

  private suspend fun extend(rule: TransactionRule, names: List<String>): PayeeRename? {
    val condition = rule.conditions.single()
    val values = (condition.value as? JsonArray).orEmpty().mapNotNull { it.stringOrNull() }
    val merged = (values + names).distinct()
    if (merged.size == values.size) return null
    val updated = condition.copy(value = JsonArray(merged.map(::JsonPrimitive)))
    budgetSync(conditionsChange(rule.id, updated))
    return PayeeRename.Extended(rule.id, oldCondition = condition)
  }

  private fun conditionsChange(rule: RuleId, condition: Condition): LocalChange =
    localChange(
      RULES,
      rule.value,
      "conditions",
      DbJson.encodeToString(RuleJson.conditions, listOf(condition)),
    )

  private fun newRule(row: String, names: List<String>, to: PayeeId): List<LocalChange> {
    val condition =
      Condition(
        field = ImportedPayee,
        operator = OneOf,
        type = ConditionType.String,
        value = JsonArray(names.map(::JsonPrimitive)),
      )
    val action = RuleAction(value = JsonPrimitive(to.value), op = Set, field = Payee, type = Id)
    return listOf(
      localChange(RULES, row, "id", row),
      localChange(RULES, row, "stage", RuleStage.Pre),
      localChange(
        RULES,
        row,
        "conditions",
        DbJson.encodeToString(RuleJson.conditions, listOf(condition)),
      ),
      localChange(RULES, row, "actions", DbJson.encodeToString(RuleJson.actions, listOf(action))),
      localChange(RULES, row, "tombstone", false),
      localChange(RULES, row, "conditions_op", ConditionOp.And),
    )
  }

  // getOneOfSetterRules('pre', 'imported_payee', 'payee', { actionValue: to })
  private fun TransactionRule.renamesTo(payee: PayeeId): Boolean {
    val action = actions.singleOrNull() ?: return false
    val condition = conditions.singleOrNull() ?: return false
    return stage == Pre &&
      action.op == Set &&
      action.field?.toRuleField() == Payee &&
      action.value?.contentOrNull == payee.value &&
      condition.operator == OneOf &&
      condition.field.toRuleField() == ImportedPayee
  }

  private fun JsonElement.stringOrNull(): String? = (this as? JsonPrimitive)?.contentOrNull
}
