package aktual.budget.transactions.domain

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.RowBounds
import aktual.budget.db.dao.TransactionDao
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.FilterBounds
import aktual.budget.rules.domain.RuleActions
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.rules.domain.TransactionFilter
import aktual.budget.rules.domain.TransactionRule
import aktual.budget.rules.domain.TransactionRulesLoader
import aktual.budget.rules.domain.splitIndex
import aktual.core.utils.UuidGenerator
import aktual.log
import dev.zacsweers.metro.Inject

/**
 * The transactions a draft rule's conditions match, and what its actions would do to them, as the
 * rule editor in upstream's RuleEditor.tsx shows them. Conditions match the way a transaction
 * filter does, so with `make-filters-from-conditions` rather than how rules run. Split parents
 * never match, and nor do split children if the rule makes splits.
 */
@Inject
class RuleMatcher(
  private val transactionDao: TransactionDao,
  private val accountDao: AccountDao,
  private val categoryDao: CategoryDao,
  private val payeeDao: PayeeDao,
  private val rulesLoader: TransactionRulesLoader,
  private val writer: TransactionWriter,
  private val uuidGenerator: UuidGenerator,
) {
  /** Null if the rule's actions are invalid */
  suspend fun find(rule: TransactionRule): RuleMatches? {
    val actions = RuleActions.compile(rule.id, rule.actions) ?: return null
    val filter = TransactionFilter.compile(rule.conditions, rule.conditionsOp)
    if (filter.isEmpty) return RuleMatches(emptyList(), emptyMap(), emptyMap())

    val makesSplits = rule.actions.any { it.splitIndex > 0 }
    val rows =
      transactionDao
        .getFilterRows(skipDeleted = false, bounds = filter.bounds.toRowBounds())
        .filter { row ->
          row.is_parent != true &&
            !(makesSplits && row.is_child == true) &&
            filter.matches(row.toFilterRow())
        }

    val rules = rulesLoader.load()
    val matches =
      rows
        .map { row ->
          val before = row.toRuleTransaction()
          RuleMatch(before, rules.run(actions, before))
        }
        .sortedByDescending { it.before.date }

    val names = buildMap {
      rows.forEach { row ->
        row.payee?.let { put(it.value, row.payee_name.orEmpty()) }
        row.category?.let { put(it.value, row.category_name.orEmpty()) }
        put(row.account.value, row.account_name.orEmpty())
      }
      putAll(rules.createdPayees.mapKeys { it.key.value })
    }
    return RuleMatches(matches, names + newNames(matches, names), rules.createdPayees)
  }

  /**
   * Runs the rule's actions on the [selected] transactions it matches, as `rule-apply-actions`
   * does, and returns how many it changed
   */
  suspend fun apply(rule: TransactionRule, selected: Set<TransactionId>): Int {
    val matches = find(rule) ?: return 0
    val changed = matches.matches.filter { it.isChanged && it.before.id in selected }
    if (changed.isEmpty()) return 0

    writer.write(runTransfers = true) {
      val usedPayees = changed.flatMap { it.after.payees() }.toSet()
      matches.createdPayees
        .filterKeys { it in usedPayees }
        .forEach { (id, name) -> insertPayee(name, id = id) }

      changed.forEach { (before, after) ->
        val id = before.id ?: return@forEach
        when {
          after.tombstone -> delete(id)
          after.subtransactions.isNotEmpty() -> {
            update(
              update(id, before, after).copy(isParent = true, error = Patch.To(after.splitError())),
            )
            after.newChildren(id) { uuidGenerator(::TransactionId) }.forEach { insert(it) }
          }
          else -> update(update(id, before, after))
        }
      }
    }
    log.i { "Applied the actions of ${rule.id} to ${changed.size} transactions" }
    return changed.size
  }

  // Names of what the actions set, which the matched rows didn't already have
  private suspend fun newNames(
    matches: List<RuleMatch>,
    known: Map<String, String>,
  ): Map<String, String> {
    val after = matches.asSequence().map { it.after }
    val payees = after.flatMap { it.payees() }.filter { it.value !in known }.distinct()
    val categories =
      after
        .flatMap { t -> sequenceOf(t.category) + t.subtransactions.map { it.category } }
        .filterNotNull()
        .filter { it.value !in known }
        .distinct()
    val accounts = after.map { it.account }.filter { it.value !in known }.distinct()
    return buildMap {
      payees.forEach { id -> payeeDao.name(id)?.let { put(id.value, it) } }
      categories.forEach { id -> categoryDao.name(id)?.let { put(id.value, it) } }
      accounts.forEach { id -> put(id.value, accountDao.name(id)) }
    }
  }

  private fun RuleTransaction.payees(): List<PayeeId> =
    listOfNotNull(payee) + subtransactions.mapNotNull { it.payee }

  private fun update(id: TransactionId, before: RuleTransaction, after: RuleTransaction) =
    TransactionUpdate(
      id = id,
      account = after.account.takeIf { it != before.account },
      date = after.date.takeIf { it != before.date },
      amount = after.amount.takeIf { it != before.amount },
      cleared = after.cleared.takeIf { it != before.cleared },
      reconciled = after.reconciled.takeIf { it != before.reconciled },
      payee = patch(before.payee, after.payee),
      category = patch(before.category, after.category),
      notes = patch(before.notes, after.notes),
      importedPayee = patch(before.importedPayee, after.importedPayee),
      schedule = patch(before.schedule, after.schedule),
    )

  private fun <T> patch(before: T?, after: T?): Patch<T?> =
    if (before == after) Keep else Patch.To(after)
}

data class RuleMatches(
  // Newest first
  val matches: List<RuleMatch>,
  // The names of the payees, categories and accounts before and after, by id
  val names: Map<String, String>,
  internal val createdPayees: Map<PayeeId, String> = emptyMap(),
)

data class RuleMatch(val before: RuleTransaction, val after: RuleTransaction) {
  val isChanged: Boolean
    get() = before != after
}

private fun FilterBounds.toRowBounds() =
  RowBounds(
    from = from,
    to = to,
    accounts = accounts?.mapTo(mutableSetOf(), ::AccountId),
    categories = categories?.mapTo(mutableSetOf(), ::CategoryId),
    payees = payees?.mapTo(mutableSetOf(), ::PayeeId),
  )
