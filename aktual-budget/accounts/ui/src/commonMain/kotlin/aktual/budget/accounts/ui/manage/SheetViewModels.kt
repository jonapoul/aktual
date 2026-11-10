package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.components.rememberSheetViewModelStoreOwner
import aktual.budget.accounts.vm.close.CloseAccountViewModel
import aktual.budget.model.AccountId
import androidx.compose.runtime.Composable
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
internal fun closeAccountViewModel(id: AccountId): CloseAccountViewModel =
  assistedMetroViewModel<CloseAccountViewModel, CloseAccountViewModel.Factory>(
    viewModelStoreOwner = rememberSheetViewModelStoreOwner(),
  ) {
    create(id)
  }
