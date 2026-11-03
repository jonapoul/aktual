package aktual.budget.reports.vm

import aktual.budget.db.ReportTransactions
import aktual.budget.db.SankeyCategoryTotals
import aktual.budget.db.SankeyTransfers

// The sankeyCategoryTotals query run over transactions that matched a report's conditions: grouped
// by category, account and payee, leaving out deleted categories, groups and accounts, and with
// deleted payees treated as no payee
internal fun sankeyCategoryTotals(rows: List<ReportTransactions>): List<SankeyCategoryTotals> =
  rows
    .filter { it.account_tombstone == false }
    .mapNotNull { row ->
      val category = row.category?.takeIf { row.category_tombstone == false }
      val group = row.category_group?.takeIf { row.group_tombstone == false }
      if (category == null || group == null) return@mapNotNull null
      val livePayee = row.payee_tombstone == false
      SankeyCategoryTotals(
        category = category,
        category_name = row.category_name,
        category_sort_order = row.category_sort_order,
        category_group = group,
        group_name = row.category_group_name,
        group_sort_order = row.group_sort_order,
        is_income = row.is_income,
        account = row.account,
        account_name = row.account_name,
        payee = row.payee?.takeIf { livePayee },
        payee_name = row.payee_name?.takeIf { livePayee },
        total = row.amount,
      )
    }
    .groupBy { Triple(it.category, it.account, it.payee) }
    .values
    .map { group -> group.first().copy(total = group.sumOf { it.total }) }

// The sankeyTransfers query run over transactions that matched a report's conditions
internal fun sankeyTransfers(rows: List<ReportTransactions>): List<SankeyTransfers> =
  rows
    .filter { it.transfer_id != null && it.account_tombstone == false && it.off_budget == false }
    .map { row ->
      SankeyTransfers(
        id = row.id,
        amount = row.amount,
        account = row.account,
        account_name = row.account_name,
        transfer_id = row.transfer_id,
      )
    }
