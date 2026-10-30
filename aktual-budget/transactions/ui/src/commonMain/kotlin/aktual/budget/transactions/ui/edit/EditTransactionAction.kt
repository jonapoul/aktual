package aktual.budget.transactions.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable internal sealed interface EditTransactionAction

internal data object NavigateBack : EditTransactionAction

// Throws away any changes and closes
internal data object DiscardChanges : EditTransactionAction

// Puts a saved transaction back to what's saved
internal data object RevertChanges : EditTransactionAction

internal data object SaveTransaction : EditTransactionAction

internal data object DeleteTransaction : EditTransactionAction

internal data object UnlockReconciled : EditTransactionAction

internal data object DismissError : EditTransactionAction

internal data object CommitAmount : EditTransactionAction

internal data object GoToBudget : EditTransactionAction

internal data object ToggleHiddenCategories : EditTransactionAction

internal data class PickingCategory(val isPicking: Boolean) : EditTransactionAction

// What changes one of the transaction's own fields
@Immutable internal sealed interface FieldAction : EditTransactionAction

@JvmInline internal value class SetAmount(val amount: Amount) : FieldAction

@JvmInline internal value class SetPayee(val id: PayeeId?) : FieldAction

@JvmInline internal value class CreatePayee(val name: String) : FieldAction

@JvmInline internal value class SetCategory(val id: CategoryId?) : FieldAction

@JvmInline internal value class SetAccount(val id: AccountId) : FieldAction

@JvmInline internal value class SetDate(val date: LocalDate) : FieldAction

@JvmInline internal value class SetNotes(val notes: String) : FieldAction

@JvmInline internal value class SetCleared(val cleared: Boolean) : FieldAction

@JvmInline internal value class CreateTag(val tag: String) : FieldAction

@JvmInline internal value class SetSchedule(val id: ScheduleId?) : FieldAction

// What changes a split and its parts
@Immutable internal sealed interface SplitAction : EditTransactionAction

internal data object SplitTransaction : SplitAction

internal data object AddSplit : SplitAction

@JvmInline internal value class RemoveSplit(val id: TransactionId) : SplitAction

@JvmInline internal value class FillRemaining(val id: TransactionId) : SplitAction

internal data class SetPartAmount(val id: TransactionId, val amount: Amount) : SplitAction

internal data class SetPartPayee(val id: TransactionId, val payee: PayeeId?) : SplitAction

internal data class CreatePartPayee(val id: TransactionId, val name: String) : SplitAction

internal data class SetPartCategory(val id: TransactionId, val category: CategoryId?) : SplitAction

internal data class SetPartNotes(val id: TransactionId, val notes: String) : SplitAction

@Immutable
internal fun interface EditTransactionActionHandler {
  operator fun invoke(action: EditTransactionAction)
}
