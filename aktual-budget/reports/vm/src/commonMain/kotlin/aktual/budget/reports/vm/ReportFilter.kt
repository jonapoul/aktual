package aktual.budget.reports.vm

import aktual.budget.db.ReportTransactions
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.rules.domain.FilterRow
import aktual.budget.rules.domain.TransactionFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

// A report's conditions, matched the way upstream's spreadsheets match them. Unset means all
internal fun reportFilter(conditions: List<Condition>?, op: ConditionOp?): TransactionFilter =
  TransactionFilter.compile(conditions.orEmpty(), op ?: And)

// The transactions between [start] and [end] that match [filter]. Reports with no conditions total
// in SQL instead, so they only need this when the filter isn't empty
internal fun ReportsDao.observeFilteredTransactions(
  filter: TransactionFilter,
  start: LocalDate,
  end: LocalDate,
): Flow<List<ReportTransactions>> =
  observeReportTransactions(start, end).map { rows ->
    rows.filter { filter.matches(it.toFilterRow()) }
  }

internal fun ReportTransactions.toFilterRow() =
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
