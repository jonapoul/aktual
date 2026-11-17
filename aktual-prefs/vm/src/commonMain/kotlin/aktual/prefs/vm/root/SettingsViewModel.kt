package aktual.prefs.vm.root

import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.prefs.vm.OpenBudgetName
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class SettingsViewModel
internal constructor(openBudgetName: OpenBudgetName, viewModelContext: ViewModelContext) :
  ViewModel(viewModelContext.newScope()) {
  // Null when no budget is open, so the budget's settings are hidden
  val budgetName: StateFlow<String?> =
    openBudgetName.observe().stateIn(viewModelScope, Eagerly, initialValue = null)
}
