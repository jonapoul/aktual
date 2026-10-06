package aktual.budget.rules.domain

import aktual.budget.model.Amount
import aktual.budget.model.ConditionOp
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.RuleStage
import aktual.budget.model.ScheduleId
import logcat.logcat

/**
 * Runs a budget's rules against transactions, the way upstream's `runRules`
 * (packages/loot-core/src/server/transactions/transaction-rules.ts) does when a transaction is
 * imported or added.
 *
 * Rules that fail upstream's validation are skipped, as upstream skips them when loading, and
 * listed in [invalidRules].
 */
class RulesEngine(
  rules: List<TransactionRule>,
  scheduleRules: Map<ScheduleId, RuleId> = emptyMap(),
  idMappings: Map<String, String> = emptyMap(),
) {
  private val compiled: List<CompiledRule>
  private val scheduleRules = scheduleRules.toMap()

  /** Rules that upstream would refuse to load, and so never run. */
  val invalidRules: List<RuleId>

  init {
    val invalid = mutableListOf<RuleId>()
    val valid = rules.mapNotNull { rule ->
      try {
        CompiledRule.compile(rule, idMappings)
      } catch (e: RuleValidationException) {
        logcat.w(e) { "Skipping invalid rule ${rule.id}" }
        invalid += rule.id
        null
      }
    }
    compiled =
      rankBy(valid, CompiledRule::id, CompiledRule::stage) { r -> r.conditions.map { it.op } }
    invalidRules = invalid.toList()
  }

  /** The valid rules, in the order they run. */
  val rankedRules: List<RuleId>
    get() = compiled.map { it.id }

  /** Runs every rule against [transaction], returning the transaction with their changes. */
  fun run(transaction: RuleTransaction, context: RuleContext): RuleTransaction {
    // Upstream narrows the rules down with two indexes first, which also skips some rules that
    // would otherwise match. See CompiledRule.isCandidate
    val rules = compiled.filter { it.isCandidate(transaction) }

    var subject =
      RuleSubject(
        transaction = transaction,
        payeeName = transaction.payee?.let(context::payeeName),
        categoryGroup = transaction.category?.let(context::categoryGroup),
        // Looked up once: a rule changing the account doesn't change what onBudget sees
        account = context.account(transaction.account),
      )
    var lastCategory = transaction.category

    // A transaction from a schedule always runs that schedule's rule, and never other schedules'
    val scheduleRule = transaction.schedule?.let(scheduleRules::get)
    val otherScheduleRules = scheduleRules.values.toSet() - setOfNotNull(scheduleRule)

    for (rule in rules) {
      when {
        scheduleRule != null && rule.id == scheduleRule ->
          subject = rule.execActions(subject, context)
        scheduleRule != null && rule.id in otherScheduleRules -> continue
        rule.evalConditions(subject) -> subject = rule.execActions(subject, context)
      }

      subject = subject.resolvePayee(context)

      // Keep category_group in step with a category an earlier rule changed
      val category = subject.transaction.category
      if (category != lastCategory) {
        subject = subject.copy(categoryGroup = category?.let(context::categoryGroup))
        lastCategory = category
      }
    }

    return subject.resolvePayee(context).transaction
  }
}

internal class CompiledRule(
  val id: RuleId,
  val stage: RuleStage?,
  private val conditionsOp: ConditionOp,
  val conditions: List<CompiledCondition>,
  private val actions: List<CompiledAction>,
) {
  init {
    actions.filterIsInstance<CompiledAction.Unsupported>().forEach { action ->
      logcat.w { "Rule $id has an unsupported action, which will be skipped: ${action.reason}" }
    }
  }

  // Rule.evalConditions in packages/loot-core/src/server/rules/rule.ts
  fun evalConditions(subject: RuleSubject): Boolean =
    when {
      conditions.isEmpty() -> false
      conditionsOp == Or -> conditions.any { it.eval(subject) }
      else -> conditions.all { it.eval(subject) }
    }

  // execActions in rule.ts
  fun execActions(subject: RuleSubject, context: RuleContext): RuleSubject {
    val parentActions = actions.filter { it.splitIndex == 0 }
    val childActions = actions.filter { it.splitIndex != 0 }
    val splitCount = maxOf(0, actions.maxOfOrNull { it.splitIndex } ?: 0) + 1

    val result = parentActions.fold(subject) { s, action -> action.exec(s) }
    return when {
      // No splits, no need to do anything else
      splitCount == 1 -> result
      // Rules with splits can't be applied to child transactions
      result.transaction.isChild -> result
      // TODO(#1681): upstream rebuilds an existing split here, dropping its first child. Leave
      // existing splits alone instead
      result.transaction.isParent ->
        result.also {
          logcat.w { "Rule $id would split ${it.transaction.id}, which is already split" }
        }
      else -> execSplitActions(childActions, result, context)
    }
  }

  // execSplitActions in rule.ts
  private fun execSplitActions(
    actions: List<CompiledAction>,
    subject: RuleSubject,
    context: RuleContext,
  ): RuleSubject {
    val original = subject.transaction
    // splitTransaction in packages/loot-core/src/shared/transactions.ts clears the parent's payee
    val parent = original.copy(isParent = true, payee = null)

    // The first child stands in for split index 0, which belongs to the parent's actions. It's
    // dropped at the end
    val children = mutableListOf(RuleSubject(makeChild(parent, payee = original.payee)))
    for (action in actions) {
      if (action.splitIndex < 1) continue
      while (action.splitIndex >= children.size) {
        // addSplitTransaction copies the payee of the split before
        val payee = children.lastOrNull()?.transaction?.payee ?: parent.payee
        children += RuleSubject(makeChild(parent, payee))
      }
      children[action.splitIndex] = action.exec(children[action.splitIndex])
    }

    val splitAmounts = actions.filterIsInstance<CompiledAction.SetSplitAmount>()
    fun remainder(): Long = parent.amount.toLong() - children.sumOf { it.amount() }

    // Fixed percentages are of what's left after the fixed amounts
    val afterFixedAmounts = remainder()
    splitAmounts
      .filter { it.method == FixedPercent }
      .forEach { action ->
        val percent = (action.value ?: 0.0) / PERCENT
        children.setAmount(action.splitIndex, jsRound(afterFixedAmounts * percent))
      }

    // Remainders share out what's left after that, with any rounding going to the last one
    val remainders = splitAmounts.filter { it.method == Remainder }
    if (remainders.isNotEmpty()) {
      val each = jsRound(remainder().toDouble() / remainders.size)
      remainders.forEach { children.setAmount(it.splitIndex, each) }
      val last = remainders.maxOf { it.splitIndex }
      children.setAmount(last, children[last].amount() + remainder())
    }

    // Upstream never resolves a payee_name set on a split, leaving its payee as "new". Look it up
    // like one set on the whole transaction instead
    val subtransactions = children.drop(1).map { it.resolvePayee(context).transaction }
    return subject.copy(transaction = parent.copy(subtransactions = subtransactions))
  }

  private fun RuleSubject.amount(): Long = transaction.amount.toLong()

  private fun MutableList<RuleSubject>.setAmount(index: Int, amount: Long) {
    val child = this[index]
    this[index] = child.copy(transaction = child.transaction.copy(amount = Amount(amount)))
  }

  // makeChild in packages/loot-core/src/shared/transactions.ts
  private fun makeChild(parent: RuleTransaction, payee: PayeeId?) =
    RuleTransaction(
      account = parent.account,
      date = parent.date,
      amount = Zero,
      payee = payee,
      category = parent.category,
      cleared = parent.cleared,
      reconciled = parent.reconciled,
      isChild = true,
    )

  /**
   * Whether upstream's rule indexes would hand this rule to `runRules` for [transaction]. Rules are
   * indexed by their first `imported_payee` condition (by first letter) and by their first `payee`
   * condition, and a rule only runs if either index returns it. So a rule with an
   * `is`/`isNot`/`oneOf`/`notOneOf` condition on both fields never runs unless one of them points
   * at the transaction's values, even in an "or" rule or with `isNot`. See `RuleIndexer` in
   * packages/loot-core/src/server/rules/rule-indexer.ts.
   */
  fun isCandidate(transaction: RuleTransaction): Boolean =
    isIndexed(ImportedPayee, transaction.importedPayee, firstChar = true) ||
      isIndexed(Payee, transaction.payee?.value, firstChar = false)

  private fun isIndexed(field: RuleField, value: String?, firstChar: Boolean): Boolean {
    val condition = conditions.firstOrNull { it.field == field }
    if (condition == null || condition.op !in INDEXED_OPS) return true

    val values =
      when (val v = condition.value) {
        is TextList -> v.values
        is Text -> listOf(v.value)
        else -> return true
      }
    val keys = values.map { indexKey(it, firstChar) ?: WILDCARD }.toSet()
    val key = indexKey(value, firstChar)
    val keyInKeys = key != null && key in keys
    return WILDCARD in keys || keyInKeys
  }

  private fun indexKey(value: String?, firstChar: Boolean): String? =
    when {
      value.isNullOrEmpty() -> null
      firstChar -> value.first().lowercase()
      else -> value.lowercase()
    }

  companion object {
    private const val PERCENT = 100.0
    private const val WILDCARD = "*"
    private val INDEXED_OPS = setOf<Operator>(Is, IsNot, OneOf, NotOneOf)

    fun compile(rule: TransactionRule, idMappings: Map<String, String>): CompiledRule =
      CompiledRule(
        id = rule.id,
        stage = rule.stage,
        conditionsOp = rule.conditionsOp,
        conditions = rule.conditions.map { CompiledCondition.compile(it, idMappings) },
        actions = rule.actions.map { CompiledAction.compile(it, idMappings) },
      )
  }
}

// resolvePayeeNameForRules in packages/loot-core/src/server/transactions/transaction-rules.ts
internal fun RuleSubject.resolvePayee(context: RuleContext): RuleSubject {
  if (!newPayeePending) return this
  val payee = payeeName?.takeIf { it.isNotEmpty() }?.let(context::resolvePayee)
  return copy(transaction = transaction.copy(payee = payee), newPayeePending = false)
}
