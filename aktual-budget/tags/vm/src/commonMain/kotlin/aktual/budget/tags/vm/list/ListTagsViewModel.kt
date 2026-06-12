package aktual.budget.tags.vm.list

import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ListTagsViewModel : ViewModel() {
  val tags: StateFlow<ImmutableList<TagItem>> =
    viewModelScope.launchMolecule(Immediate) {
      // TBC
      persistentListOf()
    }
}
