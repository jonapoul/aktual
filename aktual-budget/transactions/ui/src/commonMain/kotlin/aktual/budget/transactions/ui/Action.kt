package aktual.budget.transactions.ui

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.TagId
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.transactions.domain.SavedFilter
import aktual.budget.transactions.vm.AccountOption
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data object NavBack : Action

  @JvmInline value class EditAccount(val id: AccountId) : Action

  @JvmInline value class SwitchAccount(val id: AccountId) : Action

  data object SwitchToAllAccounts : Action

  data object BankSync : ViewModelAction

  data object DeleteAccount : ViewModelAction

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

  @JvmInline value class SetQuery(val text: String) : ViewModelAction

  @JvmInline value class AddCondition(val condition: Condition) : ViewModelAction

  data class EditCondition(val index: Int, val condition: Condition) : ViewModelAction

  @JvmInline value class RemoveCondition(val index: Int) : ViewModelAction

  @JvmInline value class SetConditionsOp(val op: ConditionOp) : ViewModelAction

  data object ClearFilters : ViewModelAction

  data object ClearConditions : ViewModelAction

  @JvmInline value class ApplySavedFilter(val filter: SavedFilter) : ViewModelAction

  @JvmInline value class SaveFilter(val name: String) : ViewModelAction

  data object UpdateSavedFilter : ViewModelAction

  @JvmInline value class RenameSavedFilter(val name: String) : ViewModelAction

  data object DeleteSavedFilter : ViewModelAction
}

// Handled by the view model alone
internal sealed interface ViewModelAction : Action
