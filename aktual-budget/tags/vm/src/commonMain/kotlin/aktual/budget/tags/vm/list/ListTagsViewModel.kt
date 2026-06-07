package aktual.budget.tags.vm.list

import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListTagsViewModel : ViewModel() {
  // TBC
}
