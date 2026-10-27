package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.vm.AccountOption
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data object NavBack : Action

  data object BankSync : Action

  data object DeleteAccount : Action

  data class SetDensity(val density: TransactionsDensity) : Action

  data class SetAlternateRowColours(val enabled: Boolean) : Action

  data class SetExpandSplits(val enabled: Boolean) : Action

  data class SetAccountOption(val option: AccountOption, val enabled: Boolean) : Action

  data class ToggleSplit(val id: TransactionId) : Action

  data class OpenTransaction(val id: TransactionId) : Action

  data object CreateTransaction : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
