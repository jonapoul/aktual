package aktual.about.vm

import aktual.budget.model.BudgetId
import aktual.core.model.Bytes
import aktual.core.model.Percent
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ManageStorageState {
  data object Loading : ManageStorageState

  @Immutable
  data class Loaded(
    val totalSize: Bytes,
    val percentTotalStorage: Percent,
    val budgets: ImmutableList<BudgetStorageItem>,
    val cacheSize: Bytes,
    val otherSize: Bytes,
    val dialog: StorageDialog = StorageDialog.None,
  ) : ManageStorageState
}

@Immutable data class BudgetStorageItem(val id: BudgetId, val name: String, val size: Bytes)

@Immutable
sealed interface StorageDialog {
  data object None : StorageDialog

  data object ConfirmClearAllFiles : StorageDialog

  data class ConfirmClearBudget(val id: BudgetId, val name: String) : StorageDialog

  data object ConfirmClearCache : StorageDialog

  data object ConfirmClearPreferences : StorageDialog
}
