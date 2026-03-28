package aktual.about.ui.storage

import aktual.budget.model.BudgetId
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ManageStorageAction {
  data object NavBack : ManageStorageAction

  data object Reload : ManageStorageAction

  data object RequestClearAllFiles : ManageStorageAction

  data class RequestClearBudget(val id: BudgetId, val name: String) : ManageStorageAction

  data object RequestClearCache : ManageStorageAction

  data object RequestClearPreferences : ManageStorageAction

  data object ConfirmClearAllFiles : ManageStorageAction

  data class ConfirmClearBudget(val id: BudgetId) : ManageStorageAction

  data object ConfirmClearCache : ManageStorageAction

  data object ConfirmClearPreferences : ManageStorageAction

  data object DismissDialog : ManageStorageAction
}
