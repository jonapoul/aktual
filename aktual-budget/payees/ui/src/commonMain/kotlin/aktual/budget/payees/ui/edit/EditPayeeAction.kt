package aktual.budget.payees.ui.edit

import aktual.budget.payees.vm.list.PayeeItem
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface EditPayeeAction

internal data object NavigateBack : EditPayeeAction

internal data object SavePayee : EditPayeeAction

internal data object DismissSaveError : EditPayeeAction

@JvmInline internal value class SetName(val name: String) : EditPayeeAction

@JvmInline internal value class SetFavorite(val isFavorite: Boolean) : EditPayeeAction

@JvmInline internal value class SetLearnCategories(val learn: Boolean) : EditPayeeAction

@JvmInline internal value class OpenRules(val payee: PayeeItem) : EditPayeeAction

// title is the payee's name as shown, for the transactions screen's top bar
@JvmInline internal value class OpenTransactions(val title: String) : EditPayeeAction

@Immutable
internal fun interface EditPayeeActionHandler {
  operator fun invoke(action: EditPayeeAction)
}
