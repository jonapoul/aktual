package aktual.budget.reports.vm

import aktual.budget.db.ForecastAccounts
import aktual.budget.db.ForecastPayees
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.rules.domain.FilterRow
import aktual.budget.rules.domain.TransactionFilter

// packages/loot-core/src/server/forecast/forecast-filters.ts buildFilterInfo()
internal class ForecastFilters(conditions: List<Condition>?, op: ConditionOp?) {
  private val plain = conditions.orEmpty().filter { it.customName == null }
  private val op = op ?: And
  val transactions: TransactionFilter = TransactionFilter.compile(plain, this.op)

  // forecast-accounts.ts getAccountRestrictionMode(): account conditions can only narrow the
  // accounts when nothing else could match a transaction in another account
  private val accountFilter: TransactionFilter? =
    plain
      .filter { it.field == Account }
      .takeIf { it.isNotEmpty() && (this.op == And || it.size == plain.size) }
      ?.let { TransactionFilter.compile(it, this.op) }

  // resolveForecastAccounts()
  fun accounts(live: List<ForecastAccounts>, selected: Set<AccountId>?): Set<AccountId> =
    live
      .asSequence()
      .filter { selected == null || it.id in selected }
      .filter { account -> accountFilter?.matches(account.toFilterRow()) != false }
      .map { it.id }
      .toSet()

  // forecast-projection.ts indexScheduleOccurrences(). Rules aren't run against the simulated
  // transactions, so they have no category
  fun matches(
    occurrence: ForecastOccurrence,
    accounts: List<ForecastAccounts>,
    payees: List<ForecastPayees>,
  ): Boolean {
    if (transactions.isEmpty) return true
    val account = accounts.firstOrNull { it.id == occurrence.account }
    val payee = payees.firstOrNull { it.id == occurrence.payee }
    return transactions.matches(
      FilterRow(
        date = occurrence.date,
        amount = Amount(occurrence.amount),
        account = occurrence.account ?: UNASSIGNED_ACCOUNT,
        accountName = account?.name.orEmpty(),
        offBudget = account?.offbudget == true,
        payee = occurrence.payee,
        payeeName = payee?.name,
        isTransfer = occurrence.isTransfer,
      ),
    )
  }

  private fun ForecastAccounts.toFilterRow() =
    FilterRow(
      date = ALL_TIME_START,
      amount = Zero,
      account = id,
      accountName = name,
      offBudget = offbudget == true,
    )

  private companion object {
    // forecast-schedules.ts FORECAST_UNASSIGNED_ACCOUNT_ID
    val UNASSIGNED_ACCOUNT = AccountId("__unassigned_schedule__")
  }
}
