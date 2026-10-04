package aktual.budget.transactions.ui

import aktual.budget.db.Accounts
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.vm.Transaction
import aktual.core.ui.ColoredParameterProvider
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.minus

private val Loaded = LoadState.NotLoading(endOfPaginationReached = true)

// Without explicit load states, static paging data is stuck on a loading refresh
internal fun previewPagingData(
  transactions: ImmutableList<Transaction>,
  loading: Boolean = false,
): Flow<PagingData<Transaction>> =
  MutableStateFlow(
    PagingData.from(
      data = transactions,
      sourceLoadStates =
        LoadStates(
          refresh = if (loading) LoadState.Loading else Loaded,
          prepend = Loaded,
          append = Loaded,
        ),
    )
  )

internal fun emptyPreviewPagingData(loading: Boolean) =
  previewPagingData(transactions = persistentListOf(), loading = loading)

internal class DensityProvider :
  ColoredParameterProvider<TransactionsDensity>(Comfortable, Compact, Dense)

internal val PREVIEW_DATE = LocalDate(2025, JUNE, 9)
internal val PREVIEW_BALANCE = Amount(3412.60)

private const val NATWEST = "NatWest"

internal val TRANSACTION_1 =
  Transaction(
    id = TransactionId("abc"),
    date = PREVIEW_DATE,
    account = NATWEST,
    payee = "Nando's",
    notes = "Cheeky!",
    category = "Food",
    amount = Amount(-21.99),
    balance = Amount(3469.60),
  )

internal val TRANSACTION_2 =
  Transaction(
    id = TransactionId("def"),
    date = PREVIEW_DATE,
    account = "Amex",
    payee = "Boots",
    notes = "Ibuprofen",
    category = "Medicine",
    amount = Amount(-3.50),
    balance = Amount(3491.59),
  )

internal val TRANSACTION_3 =
  Transaction(
    id = TransactionId("ghi"),
    date = PREVIEW_DATE,
    account = NATWEST,
    payee = "Work, Inc",
    notes = null,
    category = "Salary",
    amount = Amount(2450.00),
    balance = Amount(3495.09),
  )

internal val TRANSACTION_UNCATEGORISED =
  Transaction(
    id = TransactionId("jkl"),
    date = PREVIEW_DATE.minus(1, DAY),
    account = "Amex",
    payee = "Waterstones",
    notes = null,
    category = null,
    amount = Amount(-16.99),
    balance = Amount(1045.09),
  )

internal val PREVIEW_TRANSACTIONS =
  persistentListOf(
    TRANSACTION_1,
    TRANSACTION_2,
    TRANSACTION_3,
    TRANSACTION_UNCATEGORISED,
    Transaction(
      id = TransactionId("mno"),
      date = PREVIEW_DATE.minus(1, DAY),
      account = NATWEST,
      payee = "Landlord Ltd",
      notes = null,
      category = "Rent",
      amount = Amount(-1200.00),
      balance = Amount(1062.08),
    ),
    Transaction(
      id = TransactionId("pqr"),
      date = PREVIEW_DATE.minus(2, DAY),
      account = "Amex",
      payee = "Amazon",
      notes = "Bin bags, batteries",
      category = "Household",
      amount = Amount(-42.18),
      balance = Amount(2262.08),
    ),
    Transaction(
      id = TransactionId("stu"),
      date = PREVIEW_DATE.minus(2, DAY),
      account = NATWEST,
      payee = "Sainsbury's",
      notes = null,
      category = "Groceries",
      amount = Amount(-18.30),
      balance = Amount(2304.26),
    ),
  )

internal val PREVIEW_ACCOUNT =
  Accounts(
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
    bank_sync_status = null,
    account_group_id = null,
  )
