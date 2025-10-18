/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.preview

import actual.budget.db.Accounts
import actual.budget.model.AccountId
import actual.budget.model.Amount
import actual.budget.model.TransactionId
import actual.budget.transactions.vm.Transaction
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
