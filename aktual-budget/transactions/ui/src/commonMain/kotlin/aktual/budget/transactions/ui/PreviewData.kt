/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.ui

import aktual.budget.db.Accounts
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.transactions.vm.Transaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

internal val PREVIEW_DATE = LocalDate(2025, Month.JUNE, 9)

internal val TRANSACTION_1 = Transaction(
  id = TransactionId("abc"),
  date = PREVIEW_DATE,
  account = "NatWest",
  payee = "Nando's",
  notes = "Cheeky!",
  category = "Food",
  amount = Amount(-21.99),
)

internal val TRANSACTION_2 = Transaction(
  id = TransactionId("def"),
  date = PREVIEW_DATE,
  account = "Amex",
  payee = "Boots",
  notes = "Ibuprofen",
  category = "Medicine",
  amount = Amount(-3.50),
)

internal val TRANSACTION_3 = Transaction(
  id = TransactionId("ghi"),
  date = LocalDate(2025, Month.JUNE, 10),
  account = "NatWest",
  payee = "Work, Inc",
  notes = null,
  category = "Salary",
  amount = Amount(1234.56),
)

internal val PREVIEW_ACCOUNT = Accounts(
  id = AccountId("abc"),
  account_id = null,
  name = "My Account",
  balance_current = null,
  balance_available = null,
  balance_limit = null,
  mask = null,
  official_name = null,
  subtype = null,
  bank = null,
  offbudget = null,
  closed = null,
  tombstone = null,
  sort_order = null,
  type = null,
  account_sync_source = null,
  last_sync = null,
  last_reconciled = null,
)
