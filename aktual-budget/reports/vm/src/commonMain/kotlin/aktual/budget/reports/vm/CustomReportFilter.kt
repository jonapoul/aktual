package aktual.budget.reports.vm

import aktual.budget.db.CustomReportAmounts
import aktual.budget.db.CustomReportTransactions
import aktual.budget.model.Amount
import aktual.budget.rules.domain.FilterRow
import aktual.budget.rules.domain.TransactionFilter

// The filtered rows summed the way customReportAmounts does in SQL
internal fun filteredAmounts(
  rows: List<CustomReportTransactions>,
  filter: TransactionFilter,
): List<CustomReportAmounts> =
  rows
    .filter { filter.matches(it.toFilterRow()) }
    .groupBy { listOf(it.interval_key, it.account, it.payee, it.category, it.transfer_account) }
    .values
    .map { group ->
      val first = group.first()
      val amounts = group.map { it.amount }
      CustomReportAmounts(
        interval_key = first.interval_key,
        category = first.category,
        category_hidden = first.category_hidden,
        category_group = first.category_group,
        category_group_hidden = first.category_group_hidden,
        account = first.account,
        off_budget = first.off_budget,
        payee = first.payee,
        transfer_account = first.transfer_account,
        assets = amounts.filter { it > 0 }.sum(),
        debts = amounts.filter { it < 0 }.sum(),
      )
    }

// Each matching transaction with its notes, for reports split by tag
internal fun taggedAmounts(
  rows: List<CustomReportTransactions>,
  filter: TransactionFilter,
): List<TaggedAmounts> =
  rows
    .filter { filter.isEmpty || filter.matches(it.toFilterRow()) }
    .map { row ->
      TaggedAmounts(
        amounts =
          CustomReportAmounts(
            interval_key = row.interval_key,
            category = row.category,
            category_hidden = row.category_hidden,
            category_group = row.category_group,
            category_group_hidden = row.category_group_hidden,
            account = row.account,
            off_budget = row.off_budget,
            payee = row.payee,
            transfer_account = row.transfer_account,
            assets = row.amount.coerceAtLeast(0),
            debts = row.amount.coerceAtMost(0),
          ),
        notes = row.notes,
      )
    }

private fun CustomReportTransactions.toFilterRow() =
  FilterRow(
    date = date,
    amount = Amount(amount),
    account = account,
    accountName = account_name,
    offBudget = off_budget == true,
    payee = payee,
    payeeName = payee_name,
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
