package aktual.prefs.vm.experimental

import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ExperimentalSettingsViewModel internal constructor(viewModelContext: ViewModelContext) :
  ViewModel(viewModelContext.newScope())
