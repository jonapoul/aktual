package aktual.about.ui.storage

import aktual.budget.model.BudgetId
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ManageStorageAction

internal data object NavBack : ManageStorageAction

internal data object Reload : ManageStorageAction

internal data object RequestClearAllFiles : ManageStorageAction

internal data class RequestClearBudget(val id: BudgetId, val name: String) : ManageStorageAction

internal data object RequestClearCache : ManageStorageAction

internal data object RequestClearPreferences : ManageStorageAction

internal data object ConfirmClearAllFiles : ManageStorageAction

@JvmInline internal value class ConfirmClearBudget(val id: BudgetId) : ManageStorageAction

internal data object ConfirmClearCache : ManageStorageAction

internal data object ConfirmClearPreferences : ManageStorageAction

internal data object DismissDialog : ManageStorageAction

@Immutable
internal fun interface ManageStorageActionHandler {
  operator fun invoke(action: ManageStorageAction)
}
