package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data object NavBack : Action
  data class CheckItem(val id: TransactionId, val isChecked: Boolean) : Action
  data class SetPrivacyMode(val isPrivacyEnabled: Boolean) : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
