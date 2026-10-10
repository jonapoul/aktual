package aktual.budget.transactions.vm.edit

import aktual.budget.transactions.vm.Transaction
import androidx.compose.runtime.Immutable

// Edit and Create join View as the editor's phases land
@Immutable
sealed interface TransactionEditMode {
  data object View : TransactionEditMode
}

// A transaction as its detail screen shows it, with a split's parts as its children
@Immutable
data class TransactionDetails(
  val transaction: Transaction,
  val cleared: Boolean,
  val reconciled: Boolean,
)

@Immutable
sealed interface EditTransactionState {
  data object Loading : EditTransactionState

  sealed interface Failure : EditTransactionState {
    data object NotFound : Failure

    @JvmInline value class Other(val reason: String) : Failure
  }

  data class Loaded(
    val saved: TransactionDetails,
    val mode: TransactionEditMode,
    // Whether the edit action is offered at all. False for schedule previews, which have no row
    val canEdit: Boolean,
  ) : EditTransactionState
}
