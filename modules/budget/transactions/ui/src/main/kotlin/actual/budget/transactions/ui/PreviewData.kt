package actual.budget.transactions.ui

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
