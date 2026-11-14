package aktual.budget.transactions.domain

import aktual.budget.db.GetFilterRows
import aktual.budget.model.Amount
import aktual.budget.rules.domain.FilterRow
import aktual.budget.rules.domain.RuleTransaction

fun GetFilterRows.toFilterRow() =
  FilterRow(
    date = date,
    amount = Amount(amount),
    account = account,
    accountName = account_name,
    offBudget = off_budget == true,
    payee = payee,
    payeeName = payee_name,
    transferAccountName = transfer_account_name,
    importedPayee = imported_payee?.value,
    category = category,
    categoryName = category_name,
    categoryGroup = category_group,
    categoryGroupName = category_group_name,
    notes = notes,
    cleared = cleared == true,
    reconciled = reconciled == true,
    isTransfer = transfer_id != null,
    isParent = is_parent == true,
  )

// Without its schedule, which no condition reads
internal fun GetFilterRows.toRuleTransaction() =
  RuleTransaction(
    id = id,
    account = account,
    date = date,
    amount = Amount(amount),
    payee = payee,
    importedPayee = imported_payee?.value,
    category = category,
    notes = notes,
    cleared = cleared == true,
    reconciled = reconciled == true,
    isParent = is_parent == true,
    isChild = is_child == true,
  )
