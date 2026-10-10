package aktual.budget.transactions.vm.edit

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.transactions.domain.TransactionFields
import aktual.budget.transactions.vm.Transaction
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

// Create joins these as the editor's phases land
@Immutable
sealed interface TransactionEditMode {
  data object View : TransactionEditMode

  data class Edit(
    val draft: TransactionFields,
    val payeeName: String?,
    val categoryName: String?,
    val accountName: String?,
    // Transactions in an off budget account have no category
    val isOffBudget: Boolean,
    val hasChanges: Boolean,
    val options: TransactionOptions,
  ) : TransactionEditMode
}

// A transaction as its detail screen shows it, with a split's parts as its children
@Immutable
data class TransactionDetails(
  val transaction: Transaction,
  val cleared: Boolean,
  val reconciled: Boolean,
)

@Immutable data class EntityOption<T : Any>(val id: T, val name: String)

// A null name holds the categories that have no group
@Immutable
data class CategoryGroupOptions(
  val name: String?,
  val categories: ImmutableList<EntityOption<CategoryId>>,
)

// What the editor's pickers offer
@Immutable
data class TransactionOptions(
  val payees: ImmutableList<EntityOption<PayeeId>>,
  val categoryGroups: ImmutableList<CategoryGroupOptions>,
  val accounts: ImmutableList<EntityOption<AccountId>>,
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
    // Whether the edit and delete actions are offered at all. False for schedule previews, which
    // have no row, and for splits and transfers until the editor handles them
    val canEdit: Boolean,
    val isWorking: Boolean = false,
  ) : EditTransactionState {
    val canSave: Boolean
      get() = !isWorking && mode is Edit && mode.hasChanges
  }
}

@Immutable
sealed interface EditTransactionError {
  @JvmInline value class Saving(val reason: String) : EditTransactionError

  @JvmInline value class Deleting(val reason: String) : EditTransactionError
}

sealed interface EditTransactionEvent {
  data object Deleted : EditTransactionEvent
}
