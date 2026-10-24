package aktual.budget.transactions.vm.edit

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.transactions.domain.TransactionFields
import kotlinx.collections.immutable.toImmutableList

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
