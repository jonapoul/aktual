package aktual.budget.reports.vm

import aktual.budget.db.dao.DatabaseTables.ACCOUNTS
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.db.dao.DatabaseTables.CATEGORY_MAPPING
import aktual.budget.db.dao.DatabaseTables.PAYEES
import aktual.budget.db.dao.DatabaseTables.PAYEE_MAPPING
import aktual.budget.utils.BudgetSyncController

// Two on-budget accounts with a transfer between them, plus an off-budget account with a transfer
// into it, across April and May 2026
internal suspend fun BudgetSyncController.insertReportBudget() {
  row(ACCOUNTS, "checking", "name" to "Checking", "offbudget" to false)
  row(ACCOUNTS, "savings", "name" to "Savings", "offbudget" to false)
  row(ACCOUNTS, "invest", "name" to "Invest", "offbudget" to true)
  row(CATEGORY_GROUPS, "income", "name" to "Income", "is_income" to true)
  row(CATEGORY_GROUPS, "bills", "name" to "Bills")
  row(CATEGORIES, "salary", "name" to "Salary", "cat_group" to "income", "is_income" to true)
  row(CATEGORIES, "food", "name" to "Food", "cat_group" to "bills")
  for (id in listOf("salary", "food")) row(CATEGORY_MAPPING, id, "transferId" to id)
  row(PAYEES, "employer", "name" to "Employer")
  row(PAYEES, "to-savings", "transfer_acct" to "savings")
  row(PAYEES, "to-checking", "transfer_acct" to "checking")
  row(PAYEES, "to-invest", "transfer_acct" to "invest")
  for (id in listOf("employer", "to-savings", "to-checking", "to-invest")) {
    row(PAYEE_MAPPING, id, "targetId" to id)
  }

  transaction("april-pay", 2000, category = "salary", payee = "employer", date = 20260410)
  transaction("april-food", -400, category = "food", date = 20260420)
  transaction("pay", 1000, category = "salary", payee = "employer", date = 20260501)
  transaction("food", -300, category = "food", date = 20260510)
  transaction("out", -100, payee = "to-savings", transfer = "in", date = 20260512)
  transaction(
    "in",
    100,
    account = "savings",
    payee = "to-checking",
    transfer = "out",
    date = 20260512,
  )
  transaction("invest-out", -50, payee = "to-invest", transfer = "invest-in", date = 20260515)
  transaction("invest-in", 50, account = "invest", transfer = "invest-out", date = 20260515)
}
