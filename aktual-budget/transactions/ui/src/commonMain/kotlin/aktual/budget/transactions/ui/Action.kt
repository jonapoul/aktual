package aktual.budget.transactions.ui

import aktual.budget.model.Amount
import aktual.budget.model.TagId
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.vm.AccountOption
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data object NavBack : Action

  data object BankSync : ViewModelAction

  data object DeleteAccount : ViewModelAction

  @JvmInline value class RenameAccount(val name: String) : ViewModelAction

  data class SetDensity(val density: TransactionsDensity) : ViewModelAction

  data class SetAlternateRowColours(val enabled: Boolean) : ViewModelAction

  data class SetExpandSplits(val enabled: Boolean) : ViewModelAction

  data class SetAccountOption(val option: AccountOption, val enabled: Boolean) : ViewModelAction

  data class ToggleSplit(val id: TransactionId) : ViewModelAction

  data class OpenTransaction(val id: TransactionId) : Action

  @JvmInline value class OpenTag(val id: TagId) : Action

  data object CreateTransaction : Action

  @JvmInline value class StartReconciling(val target: Amount) : ViewModelAction

  // Tapped in a row's cleared column while reconciling
  data class ToggleCleared(val id: TransactionId, val cleared: Boolean, val reconciled: Boolean) :
    Action

  data object CreateReconcileAdjustment : Action

  data object FinishReconciling : ViewModelAction
}

// Handled by the view model alone
internal sealed interface ViewModelAction : Action
