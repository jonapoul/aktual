package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.YearMonth

@Immutable
data class NetWorthAccount(
  val id: AccountId,
  val name: String,
  val balances: ImmutableMap<YearMonth, Amount>,
)

// One account's change in a month, or its balance before the range when month is null
internal data class NetWorthRow(
  val account: AccountId,
  val name: String?,
  val month: YearMonth?,
  val total: Long,
)

// packages/desktop-client/src/components/reports/spreadsheets/net-worth-spreadsheet.ts
// recalculate()
// and NetWorthGraph.tsx: each account's running balance, leaving out accounts that stay at zero, in
// stacking order (smallest total over the range first)
internal fun netWorthData(
  title: String?,
  mode: NetWorthMode?,
  months: List<YearMonth>,
  rows: List<NetWorthRow>,
): NetWorthData {
  val accounts =
    rows
      .groupBy { it.account }
      .entries
      .asSequence()
      .map { (id, accountRows) ->
        val changes = accountRows.filter { it.month != null }.associate { it.month to it.total }
        var balance = accountRows.filter { it.month == null }.sumOf { it.total }
        val balances = months.associateWith { month ->
          balance += changes[month] ?: 0L
          Amount(balance)
        }
        NetWorthAccount(
          id = id,
          name = accountRows.firstNotNullOfOrNull { it.name }.orEmpty(),
          balances = balances.toImmutableMap(),
        )
      }
      .filter { account -> account.balances.values.any { it != Zero } }
      .sortedWith(
        compareBy<NetWorthAccount> { account -> account.balances.values.sumOf { it.toLong() } }
          .thenBy { it.name },
      )
      .toList()

  val items = months.associateWith { month ->
    accounts.fold(Amount.Zero) { sum, account -> sum + account.balances.getValue(month) }
  }

  return NetWorthData(
    title = title,
    items = items.toImmutableMap(),
    mode = if (mode == Stacked) Stacked else Trend,
    accounts = accounts.toImmutableList(),
  )
}
