package aktual.budget.transactions.vm.edit

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.transactions.domain.TransactionFields
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

// What the editor knows of the budget's accounts, payees and categories
internal data class EditorEntities(
  val options: TransactionOptions,
  val payeeNames: Map<PayeeId, String>,
  val categoryNames: Map<CategoryId, String>,
  val accountNames: Map<AccountId, String>,
  val offBudget: Set<AccountId>,
  // The account each transfer payee stands in for
  val transferAccounts: Map<PayeeId, AccountId>,
)

// What the editor offers, as it's saved now
internal suspend fun loadEditorEntities(
  payeeDao: PayeeDao,
  categoryDao: CategoryDao,
  accountDao: AccountDao,
  today: LocalDate,
): EditorEntities {
  val allPayees = payeeDao.getAllWithTransfer()
  val transferAccounts =
    allPayees.mapNotNull { row -> row.transfer_acct?.let { row.id to it } }.toMap()
  val payees = allPayees.mapNotNull { row -> row.name?.let { EntityOption(row.id, it) } }
  val ordinary = payees.filter { it.id !in transferAccounts }
  val suggested =
    suggestedPayees(
      payees = ordinary,
      favorites = allPayees.filter { it.favorite == true }.map { it.id }.toSet(),
      common = payeeDao.common(since = today.minus(COMMON_PAYEE_PERIOD), COMMON_PAYEE_LIMIT),
    )
  val groups = categoryDao.getAllGrouped().toCategoryGroups()
  val accounts = accountDao.getAllWithStatus()
  val closed = accounts.filter { it.closed == true }.map { it.id }.toSet()
  return EditorEntities(
    options =
      TransactionOptions(
        payees = ordinary.filter { it !in suggested }.toImmutableList(),
        suggestedPayees = suggested.toImmutableList(),
        // As upstream, there's no starting a transfer to a closed account
        transfers =
          payees
            .filter { transferAccounts[it.id]?.let { id -> id !in closed } == true }
            .toImmutableList(),
        categoryGroups = groups.toImmutableList(),
        hasHiddenCategories = groups.any { group -> group.categories.any { it.isHidden } },
        accounts =
          accounts
            .filter { it.id !in closed }
            .mapNotNull { row -> row.name?.let { EntityOption(row.id, it) } }
            .toImmutableList(),
        closedAccounts =
          accounts
            .filter { it.id in closed }
            .mapNotNull { row -> row.name?.let { EntityOption(row.id, it) } }
            .toImmutableList(),
      ),
    payeeNames = payees.associate { it.id to it.name },
    categoryNames = groups.flatMap { it.categories }.associate { it.id to it.name },
    accountNames = accounts.mapNotNull { row -> row.name?.let { row.id to it } }.toMap(),
    offBudget = accounts.filter { it.offbudget == true }.map { it.id }.toSet(),
    transferAccounts = transferAccounts,
  )
}

// As upstream's isBudgetTransfer, though it doesn't look at the account the transfer is from. An
// off budget one shows as that instead
internal fun EditorEntities.isBudgetTransfer(account: AccountId?, payee: PayeeId?): Boolean {
  val to = payee?.let(transferAccounts::get) ?: return false
  return account !in offBudget && to !in offBudget
}

// An account can't transfer to itself
internal fun TransactionOptions.withoutTransfersTo(
  account: AccountId?,
  entities: EditorEntities,
): TransactionOptions {
  if (account == null) return this
  val others = transfers.filter { entities.transferAccounts[it.id] != account }
  return if (others.size == transfers.size) this else copy(transfers = others.toImmutableList())
}

// As upstream's getPayeeSuggestions, the favourites and then the most used, up to five in all.
// Each lot is in alphabetical order
internal fun suggestedPayees(
  payees: List<EntityOption<PayeeId>>,
  favorites: Set<PayeeId>,
  common: List<PayeeId>,
): List<EntityOption<PayeeId>> {
  val byName = compareBy(String.CASE_INSENSITIVE_ORDER, EntityOption<PayeeId>::name)
  val favorite = payees.filter { it.id in favorites }.sortedWith(byName)
  val byId = payees.associateBy { it.id }
  val mostUsed =
    common
      .asSequence()
      .mapNotNull { id -> byId[id]?.takeIf { id !in favorites } }
      .take((MAX_SUGGESTED_PAYEES - favorite.size).coerceAtLeast(0))
      .sortedWith(byName)
      .toList()
  return favorite + mostUsed
}

// As upstream's getCommonPayees
internal val COMMON_PAYEE_PERIOD = DatePeriod(days = 12 * 7)
internal const val COMMON_PAYEE_LIMIT = 10L
private const val MAX_SUGGESTED_PAYEES = 5

internal fun TransactionFields.toRuleTransaction() =
  RuleTransaction(
    account = account ?: NO_ACCOUNT,
    date = date,
    amount = amount,
    payee = payee,
    category = category,
    notes = notes,
    cleared = cleared,
    reconciled = reconciled,
  )

// A rule that splits the transaction moves its payee to the parts it makes. They aren't used if
// the transaction is split already, so then the payee stays
internal fun TransactionFields.afterRules(result: RuleTransaction, keepPayee: Boolean) =
  copy(
    account = result.account.takeIf { it != NO_ACCOUNT },
    date = result.date,
    amount = result.amount,
    payee = if (result.isParent && keepPayee) payee else result.payee,
    category = result.category,
    notes = result.notes,
    cleared = result.cleared,
  )

// Upstream runs rules on a new transaction before it has an account, which RuleTransaction needs
private val NO_ACCOUNT = AccountId("")
