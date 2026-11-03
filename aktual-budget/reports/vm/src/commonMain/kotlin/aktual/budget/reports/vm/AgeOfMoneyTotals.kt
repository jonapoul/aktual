package aktual.budget.reports.vm

import aktual.budget.db.ReportTransactions
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Operator
import aktual.budget.rules.domain.TransactionFilter

// The ageOfMoneyTransactions query run over transactions that matched a report's conditions.
// Filtered by account, transfers to an on-budget account outside the filter count as money in or
// out too, like upstream's buildTransferInclusionFilter() in
// packages/desktop-client/src/components/reports/spreadsheets/age-of-money-spreadsheet.ts
internal fun ageOfMoneyTransactions(
  rows: List<ReportTransactions>,
  conditions: List<Condition>?,
  op: ConditionOp?,
): List<AgeOfMoneyTransaction> {
  val counterparts = counterpartFilter(conditions.orEmpty(), op ?: And)
  return rows
    .asSequence()
    .filter { row ->
      row.off_budget == false && row.amount != 0L && row.countsTowardsAge(counterparts)
    }
    .sortedBy { it.date }
    .map { AgeOfMoneyTransaction(it.date, it.amount) }
    .toList()
}

private fun ReportTransactions.countsTowardsAge(counterparts: TransactionFilter?): Boolean {
  val counterpart = transfer_acct
  if (counterpart == null || transfer_off_budget == true) return true
  if (counterparts == null || transfer_off_budget == null) return false
  val row =
    toFilterRow()
      .copy(account = counterpart, accountName = transfer_account_name, offBudget = false)
  return !counterparts.matches(row)
}

// Matches counterpart accounts inside the filtered set. Null when there are no account conditions,
// or one upstream can't invert, so on-budget transfers are left out as usual
private fun counterpartFilter(conditions: List<Condition>, op: ConditionOp): TransactionFilter? {
  val accountConditions = conditions.filter { it.customName == null && it.field == Account }
  if (accountConditions.isEmpty() || accountConditions.any { it.operator !in INVERTIBLE }) {
    return null
  }
  return TransactionFilter.compile(accountConditions, op)
}

private val INVERTIBLE =
  setOf<Operator>(Is, IsNot, OneOf, NotOneOf, OnBudget, OffBudget, Contains, DoesNotContain)
