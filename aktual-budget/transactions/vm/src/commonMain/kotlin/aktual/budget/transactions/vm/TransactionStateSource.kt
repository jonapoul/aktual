package aktual.budget.transactions.vm

import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow

@Immutable
interface TransactionStateSource {
  fun isChecked(id: TransactionId): Flow<Boolean>

  fun transactionState(id: TransactionId): Flow<TransactionState>
}
