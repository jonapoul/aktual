package aktual.budget.accounts.vm

import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class AddAccountViewModel(server: BudgetServer) : ViewModel() {
  // Bank sync goes through the server, so without one there's nothing to choose between
  val canBankSync: Boolean = server is Remote
}
