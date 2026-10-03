package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.ConditionOptions
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal val ACCOUNT = AccountId("account-1")
internal val OFF_BUDGET_ACCOUNT = AccountId("account-off")

internal fun tx(
  date: String = "2020-01-01",
  amount: Long = 0,
  payee: String? = null,
  importedPayee: String? = null,
  category: String? = null,
  notes: String? = null,
  cleared: Boolean = true,
  account: AccountId = ACCOUNT,
) =
  RuleTransaction(
    account = account,
    date = LocalDate.parse(date),
    amount = Amount(amount),
    payee = payee?.let(::PayeeId),
    importedPayee = importedPayee,
    category = category?.let(::CategoryId),
    notes = notes,
    cleared = cleared,
  )

// Mirrors the JS values upstream's tests pass straight to Condition and Action
internal fun json(value: Any?): JsonElement =
  when (value) {
    null -> JsonNull
    is JsonElement -> value
    is String -> JsonPrimitive(value)
    is Number -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is List<*> -> JsonArray(value.map(::json))
    is Map<*, *> -> JsonObject(value.entries.associate { (k, v) -> k.toString() to json(v) })
    else -> error("Unsupported JSON value $value")
  }

internal fun cond(
  field: Field,
  op: Operator,
  value: Any?,
  options: ConditionOptions? = null,
): Condition = Condition(field = field, operator = op, value = json(value), options = options)

internal fun action(
  op: RuleAction.Op,
  field: Field? = null,
  value: Any? = null,
  options: RuleAction.Options? = null,
): RuleAction =
  RuleAction(value = json(value) as JsonPrimitive, op = op, field = field, options = options)

internal fun set(field: Field, value: Any?, splitIndex: Int? = null) =
  action(RuleAction.Op.Set, field, value, splitIndex?.let { RuleAction.Options(splitIndex = it) })

internal fun splitAmount(splitIndex: Int, method: RuleAction.Method, value: Any? = null) =
  action(
    SetSplitAmount,
    value = value,
    options = RuleAction.Options(method = method, splitIndex = splitIndex),
  )

internal fun rule(
  id: String,
  conditions: List<Condition>,
  actions: List<RuleAction> = emptyList(),
  stage: RuleStage? = null,
  conditionsOp: ConditionOp = And,
) = TransactionRule(RuleId(id), conditions, actions, stage, conditionsOp)

internal fun context(
  payees: List<RulePayee> = emptyList(),
  categoryGroups: Map<CategoryId, CategoryGroupId?> = emptyMap(),
  accounts: List<RuleAccount> =
    listOf(
      RuleAccount(ACCOUNT, offBudget = false),
      RuleAccount(OFF_BUDGET_ACCOUNT, offBudget = true),
    ),
  uuids: Iterator<String> = generateSequence(1) { it + 1 }.map { "new-payee-$it" }.iterator(),
) = SnapshotRuleContext(accounts, payees, categoryGroups, uuidGenerator = { uuids.next() })

internal fun compile(condition: Condition) = CompiledCondition.compile(condition, emptyMap())

// Condition.eval against a transaction, with the lookups upstream adds before running rules
internal fun Condition.eval(transaction: RuleTransaction, context: RuleContext = context()) =
  compile(this)
    .eval(
      RuleSubject(
        transaction = transaction,
        payeeName = transaction.payee?.let(context::payeeName),
        categoryGroup = transaction.category?.let(context::categoryGroup),
        account = context.account(transaction.account),
      )
    )

// Rule.exec: runs a single rule's actions if its conditions match
internal fun TransactionRule.exec(
  transaction: RuleTransaction,
  context: RuleContext = context(),
): RuleTransaction = RulesEngine(listOf(this)).run(transaction, context)
