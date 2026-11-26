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
  override suspend fun add(fromNames: Collection<String>, to: PayeeId) {
    val names = fromNames.filter { it.isNotEmpty() }
    if (names.isEmpty()) return
    val existing = rulesDao.getAll().map { it.toTransactionRule() }.firstOrNull { it.renamesTo(to) }
    val changes = if (existing == null) newRule(names, to) else extend(existing, names)
    budgetSync(changes)
    log.i { "Added ${names.size} payee renames to $to" }
  }

  private fun extend(rule: TransactionRule, names: List<String>): List<LocalChange> {
    val condition = rule.conditions.single()
    val values = (condition.value as? JsonArray).orEmpty().mapNotNull { it.stringOrNull() }
    val merged = (values + names).distinct().map(::JsonPrimitive)
    val updated = condition.copy(value = JsonArray(merged))
    return listOf(
      localChange(
        RULES,
        rule.id.value,
        "conditions",
        DbJson.encodeToString(RuleJson.conditions, listOf(updated)),
      ),
    )
  }

  private fun newRule(names: List<String>, to: PayeeId): List<LocalChange> {
    val row = uuidGenerator(::RuleId).value
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

  private fun JsonElement.stringOrNull(): String? =
    (this as? JsonPrimitive)?.contentOrNull
}
