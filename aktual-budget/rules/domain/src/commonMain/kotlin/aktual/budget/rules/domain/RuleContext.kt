package aktual.budget.rules.domain

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.core.UuidGenerator

/**
 * What the engine needs to know about the rest of the budget while running rules: the lookups that
 * upstream's `prepareTransactionForRules` and `resolvePayeeNameForRules` make against the database.
 */
interface RuleContext {
  // For the onBudget/offBudget conditions
  fun account(id: AccountId): RuleAccount?

  // The payee's name, for payee_name conditions
  fun payeeName(id: PayeeId): String?

  // The category's group, for category_group conditions
  fun categoryGroup(id: CategoryId): CategoryGroupId?

  /**
   * The payee a "set payee_name" action renames the transaction to. Upstream looks up an active
   * payee with that name (case-insensitively) and creates one if there isn't any.
   */
  fun resolvePayee(name: String): PayeeId
}

data class RuleAccount(val id: AccountId, val offBudget: Boolean)

data class RulePayee(val id: PayeeId, val name: String?, val tombstone: Boolean = false)

/**
 * A [RuleContext] over a snapshot of the budget. Payees that [resolvePayee] has to create aren't
 * written anywhere: they get a fresh id, which later lookups by the same name return, and are
 * listed in [createdPayees] for the caller to save alongside the transactions. Not thread-safe.
 */
class SnapshotRuleContext(
  accounts: Collection<RuleAccount>,
  payees: Collection<RulePayee>,
  private val categoryGroups: Map<CategoryId, CategoryGroupId?>,
  private val uuidGenerator: UuidGenerator,
) : RuleContext {
  private val accounts = accounts.associateBy { it.id }
  private val payeeNames = payees.associate { it.id to it.name }.toMutableMap()

  // getPayeeByName: the first non-deleted payee whose lowercased name matches
  private val payeesByName =
    payees
      .filter { !it.tombstone && it.name != null }
      .reversed()
      .associate { it.name.orEmpty().lowercase() to it.id }
      .toMutableMap()

  private val mutableCreatedPayees = linkedMapOf<PayeeId, String>()

  // Payees that resolvePayee had to create, with their names
  val createdPayees: Map<PayeeId, String>
    get() = mutableCreatedPayees.toMap()

  override fun account(id: AccountId): RuleAccount? = accounts[id]

  override fun payeeName(id: PayeeId): String? = payeeNames[id]

  override fun categoryGroup(id: CategoryId): CategoryGroupId? = categoryGroups[id]

  override fun resolvePayee(name: String): PayeeId =
    payeesByName.getOrPut(name.lowercase()) {
      val id = uuidGenerator(::PayeeId)
      mutableCreatedPayees[id] = name
      payeeNames[id] = name
      id
    }
}
