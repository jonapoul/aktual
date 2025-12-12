package aktual.budget.transactions.ui

import aktual.budget.db.Accounts
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.TransactionIdSource
import aktual.budget.transactions.vm.TransactionState
import aktual.budget.transactions.vm.TransactionStateSource
import androidx.compose.runtime.Immutable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.plus

@Immutable
internal class PreviewTransactionStateSource(
  private val states: List<Pair<TransactionState, Boolean>>,
) : TransactionStateSource {
  constructor(states: Collection<TransactionState>) : this(states.map { it to false })

  constructor(vararg states: Pair<TransactionState, Boolean>) : this(states.toList())

  override fun isChecked(id: TransactionId): Flow<Boolean> = flowOf(find(id).second)

  override fun transactionState(id: TransactionId): Flow<TransactionState> = flowOf(find(id).first)

  private fun find(id: TransactionId): Pair<TransactionState, Boolean> =
    requireNotNull(states.firstOrNull { it.first.id == id }) {
      "No transaction matching $id in $states"
    }
}

internal fun previewTransactionStateSource(vararg transactions: Pair<Transaction, Boolean>) =
  PreviewTransactionStateSource(transactions.map { (t, bool) -> TransactionState.Loaded(t) to bool })

internal fun previewTransactionStateSource(vararg transactions: Transaction) =
  PreviewTransactionStateSource(transactions.map { t -> TransactionState.Loaded(t) to false })

internal fun previewTransactionStateSource(transactions: List<Transaction>) =
  PreviewTransactionStateSource(transactions.map { t -> TransactionState.Loaded(t) to false })

@Immutable
internal class PreviewTransactionIdSource(
  transactions: List<Transaction>,
) : TransactionIdSource {
  override val pagingData: Flow<PagingData<TransactionId>> =
    flowOf(PagingData.from(transactions.map { it.id }))
}

internal val PREVIEW_DATE = LocalDate(2025, JUNE, 9)

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
  date = PREVIEW_DATE.plus(1, DAY),
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
