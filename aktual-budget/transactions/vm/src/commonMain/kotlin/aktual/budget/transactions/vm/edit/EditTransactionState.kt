package aktual.budget.transactions.vm.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TransactionId
import aktual.budget.transactions.domain.SplitPart
import aktual.budget.transactions.domain.TransactionFields
import aktual.budget.transactions.vm.Transaction
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// What the editor opens: a saved transaction, or a new one starting in the account or category of
// the list it was added from
sealed interface EditTransactionTarget {
  data class Existing(val id: TransactionId) : EditTransactionTarget

  data class New(val account: AccountId? = null, val category: CategoryId? = null) :
    EditTransactionTarget
}

@Immutable
sealed interface TransactionEditMode {
  data object View : TransactionEditMode

  sealed interface Editing : TransactionEditMode {
    val form: TransactionForm
  }

  data class Edit(override val form: TransactionForm) : Editing

  data class Create(override val form: TransactionForm) : Editing
}

@Immutable
data class TransactionForm(
  val draft: TransactionFields,
  val payeeName: String?,
  val categoryName: String?,
  val accountName: String?,
  // Transactions in an off budget account have no category
  val isOffBudget: Boolean,
  // Nor do transfers to another on budget account
  val isTransfer: Boolean = false,
  val hasChanges: Boolean,
  val options: TransactionOptions,
  // Empty unless the transaction is split
  val parts: ImmutableList<SplitPartForm> = persistentListOf(),
  // What's left to assign while a split's parts don't add up to its amount
  val splitRemaining: Amount? = null,
) {
  val isSplit: Boolean
    get() = parts.isNotEmpty()
}

@Immutable
data class SplitPartForm(
  val part: SplitPart,
  val payeeName: String?,
  val categoryName: String?,
  // A transfer to another on budget account, which has no category
  val isTransfer: Boolean = false,
)

// A transaction as its detail screen shows it, with a split's parts as its children
@Immutable
data class TransactionDetails(
  val transaction: Transaction,
  val cleared: Boolean,
  val reconciled: Boolean,
  // Whether it, or a part of it, is a transfer whose other side is reconciled
  val hasReconciledTransfer: Boolean = false,
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
  // The payees that transfer to another account, named after it
  val transfers: ImmutableList<EntityOption<PayeeId>>,
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

  // A new transaction needs an account to go in and, as upstream has it, a category to pick
  sealed interface Empty : EditTransactionState {
    data object NoAccounts : Empty

    data object NoCategories : Empty
  }

  data class Loaded(
    // Null for a new transaction
    val saved: TransactionDetails?,
    val mode: TransactionEditMode,
    val isWorking: Boolean = false,
  ) : EditTransactionState {
    // A split can't be saved until its parts add up
    val canSave: Boolean
      get() =
        !isWorking &&
          when (mode) {
            View -> false
            is Edit -> mode.form.hasChanges && mode.form.splitRemaining == null
            is Create -> mode.form.draft.account != null && mode.form.splitRemaining == null
          }
  }
}

@Immutable
sealed interface EditTransactionError {
  @JvmInline value class Saving(val reason: String) : EditTransactionError

  @JvmInline value class Deleting(val reason: String) : EditTransactionError
}

// Each of these closes the screen
sealed interface EditTransactionEvent {
  data object Deleted : EditTransactionEvent

  data object Created : EditTransactionEvent

  data object Discarded : EditTransactionEvent
}
