package aktual.budget.transactions.ui

import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data object NavBack : Action

  data object BankSync : Action

  data object OpenSettings : Action

  data class SetDensity(val density: TransactionsDensity) : Action

  data class ToggleSplit(val id: TransactionId) : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
