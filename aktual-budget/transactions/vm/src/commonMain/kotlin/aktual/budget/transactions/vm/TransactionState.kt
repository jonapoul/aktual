package aktual.budget.transactions.vm

import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable

@Immutable
sealed interface TransactionState {
  val id: TransactionId

  data class Loaded(val transaction: Transaction) : TransactionState {
    override val id: TransactionId
      get() = transaction.id
  }

  data class Loading(override val id: TransactionId) : TransactionState

  data class DoesntExist(override val id: TransactionId) : TransactionState
}
