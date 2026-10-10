package aktual.budget.transactions.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable internal sealed interface EditTransactionAction

internal data object NavigateBack : EditTransactionAction

internal data object StartEditing : EditTransactionAction

internal data object StopEditing : EditTransactionAction

internal data object SaveTransaction : EditTransactionAction

internal data object DeleteTransaction : EditTransactionAction

internal data object UnlockReconciled : EditTransactionAction

internal data object DismissError : EditTransactionAction

@JvmInline internal value class SetAmount(val amount: Amount) : EditTransactionAction

@JvmInline internal value class SetPayee(val id: PayeeId?) : EditTransactionAction

@JvmInline internal value class SetCategory(val id: CategoryId?) : EditTransactionAction

@JvmInline internal value class SetAccount(val id: AccountId) : EditTransactionAction

@JvmInline internal value class SetDate(val date: LocalDate) : EditTransactionAction

@JvmInline internal value class SetNotes(val notes: String) : EditTransactionAction

@JvmInline internal value class SetCleared(val cleared: Boolean) : EditTransactionAction

@Immutable
internal fun interface EditTransactionActionHandler {
  operator fun invoke(action: EditTransactionAction)
}
