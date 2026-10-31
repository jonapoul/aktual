package aktual.budget.rules.domain

import aktual.budget.BudgetSyncController
import aktual.budget.db.DbJson
import aktual.budget.db.dao.CategoryLearningDao
import aktual.budget.db.dao.DatabaseTables.RULES
import aktual.budget.db.dao.LearningTransaction
import aktual.budget.db.dao.RulesDao
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.LocalChange
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleAction.Op.Set
import aktual.budget.model.RuleId
import aktual.budget.model.TransactionId
import aktual.budget.model.localChange
import aktual.budget.model.serialName
import aktual.core.Calendar
import aktual.core.UuidGenerator
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import logcat.logcat

/**
 * updateCategoryRules() in packages/loot-core/src/server/transactions/transaction-rules.ts. When
 * one category wins among a payee's latest transactions, the payee's simple `payee is X -> set
 * category` rules are pointed at it, or one is added. Does nothing while the learn-categories pref
 * is off.
 */
fun interface CategoryLearner {
  // [ids] are the transactions that were just added or given a category
  suspend fun learn(ids: Collection<TransactionId>)
}

@ContributesBinding(BudgetScope::class)
class CategoryLearnerImpl
internal constructor(
  private val learningDao: CategoryLearningDao,
  private val rulesDao: RulesDao,
  private val pref: CategoryLearningPref,
  private val syncController: BudgetSyncController,
  private val uuidGenerator: UuidGenerator,
  private val calendar: Calendar,
) : CategoryLearner {
  override suspend fun learn(ids: Collection<TransactionId>) {
    if (ids.isEmpty() || !pref.isEnabled()) return
    val changed = learningDao.changed(ids)
    val payees = changed.mapNotNullTo(LinkedHashSet()) { it.payee }
    val oldest = changed.minOfOrNull { it.date }
    if (payees.isEmpty() || oldest == null) return

    val changedIds = changed.mapTo(HashSet()) { it.id }
    val history =
      learningDao
        .history(
          payees = payees,
          from = oldest.minus(WINDOW_DAYS, DAY),
          to = calendar.today().plus(WINDOW_DAYS, DAY),
        )
        .groupBy { it.payee }

    val learned = buildMap {
      for (payee in payees) {
        val latest = history[payee].orEmpty().take(LATEST)
        if (latest.none { it.id in changedIds }) continue
        probableCategory(latest)?.let { put(payee, it) }
      }
    }
    if (learned.isEmpty()) return

    val rules = rulesDao.getAll().map { it.toTransactionRule() }
    val changes = learned.flatMap { (payee, category) -> ruleChanges(rules, payee, category) }
    if (changes.isEmpty()) return
    syncController.syncChanges(changes)
    logcat.i { "Learned categories for ${learned.size} payees" }
  }

  private fun ruleChanges(
    rules: List<TransactionRule>,
    payee: PayeeId,
    category: CategoryId,
  ): List<LocalChange> {
    val setters = rules.filter { it.isSetter(payee) }
    return when {
      // Two clients can each add one, so all of them are updated
      setters.isNotEmpty() ->
        setters
          .filter { it.actions.single().value?.contentOrNull != category.value }
          .map { rule ->
            val action = rule.actions.single().copy(value = JsonPrimitive(category.value))
            localChange(RULES, rule.id.value, "actions", DbJson.encodeToString(listOf(action)))
          }
      rules.any { it.isOneOfSetter(payee, category) } -> emptyList()
      else -> newRule(payee, category)
    }
  }

  private fun newRule(payee: PayeeId, category: CategoryId): List<LocalChange> {
    val row = uuidGenerator(::RuleId).value
    val condition =
      Condition(
        field = Payee,
        operator = Is,
        type = Id,
        value = JsonPrimitive(payee.value),
      )
    val action =
      RuleAction(
        value = JsonPrimitive(category.value),
        op = Set,
        field = Category,
        type = Id,
      )
    return listOf(
      localChange(RULES, row, "id", row),
      localChange(RULES, row, "stage", null as String?),
      localChange(RULES, row, "conditions", DbJson.encodeToString(listOf(condition))),
      localChange(RULES, row, "actions", DbJson.encodeToString(listOf(action))),
      localChange(RULES, row, "tombstone", false),
      localChange(RULES, row, "conditions_op", ConditionOp.And.serialName()),
    )
  }
}

/**
 * getProbableCategory(): the most common category, if it appears at least [MIN_SCORE] times. Ties
 * go to the newest. As upstream, an uncategorised newest transaction stops any learning, since its
 * score never compares as lower.
 */
internal fun probableCategory(transactions: List<LearningTransaction>): CategoryId? {
  val scores = transactions.mapNotNull { it.category }.groupingBy { it }.eachCount()
  val first = transactions.firstOrNull() ?: return null
  var winner = first.category
  var winnerScore = winner?.let(scores::getValue) ?: return null
  for ((_, _, category) in transactions.drop(1)) {
    val score = category?.let(scores::getValue) ?: continue
    if (score > winnerScore) {
      winner = category
      winnerScore = score
    }
  }
  return if (winnerScore >= MIN_SCORE) winner else null
}

// getIsSetterRules(): an unstaged rule with one `payee is/isNot X` condition and one `set category`
private fun TransactionRule.isSetter(payee: PayeeId): Boolean {
  val condition = conditions.singleOrNull() ?: return false
  return isCategorySetter() &&
    (condition.operator == Is || condition.operator == IsNot) &&
    condition.isOnPayee() &&
    condition.value.stringOrNull() == payee.value
}

// getOneOfSetterRules(): as above with a `payee oneOf [..X..]` condition, setting [category]
private fun TransactionRule.isOneOfSetter(payee: PayeeId, category: CategoryId): Boolean {
  val condition = conditions.singleOrNull() ?: return false
  val payees = (condition.value as? JsonArray).orEmpty().map { it.stringOrNull() }
  return isCategorySetter() &&
    actions.single().value?.contentOrNull == category.value &&
    condition.operator == OneOf &&
    condition.isOnPayee() &&
    payee.value in payees
}

private fun TransactionRule.isCategorySetter(): Boolean {
  val action = actions.singleOrNull() ?: return false
  // Aktual's rule editor saves the default stage as "default", upstream as null
  val isUnstaged = stage == null || stage == Default
  return isUnstaged && action.op == Set && action.field?.toRuleField() == Category
}

private fun Condition.isOnPayee(): Boolean = field.toRuleField() == Payee

private fun JsonElement.stringOrNull(): String? = (this as? JsonPrimitive)?.contentOrNull

// transaction-rules.ts looks this far either side of the changed transactions
private const val WINDOW_DAYS = 180

private const val LATEST = 5

private const val MIN_SCORE = 3
