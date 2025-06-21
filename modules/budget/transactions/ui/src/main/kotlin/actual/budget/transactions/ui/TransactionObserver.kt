package actual.budget.transactions.ui

import actual.budget.model.TransactionId
import actual.budget.transactions.vm.Transaction
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Immutable
internal fun interface TransactionObserver {
  operator fun invoke(id: TransactionId): Flow<Transaction>
}

internal fun previewObserver(vararg transactions: Transaction) = TransactionObserver { id ->
  val transaction = transactions.firstOrNull { it.id == id } ?: error("No match for $id: $transactions")
  flowOf(transaction)
}
