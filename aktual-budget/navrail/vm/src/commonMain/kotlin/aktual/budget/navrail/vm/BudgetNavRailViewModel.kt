package aktual.budget.navrail.vm

import aktual.budget.navrail.vm.edit.DEFAULT_NAV_GRID_ORDER
import aktual.budget.navrail.vm.edit.reconcileNavGridOrder
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetTab
import aktual.di.BudgetScope
import aktual.prefs.AppPreferences
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class BudgetNavRailViewModel(
  contributors: Set<BudgetNavEntryContributor>,
  prefs: AppPreferences,
) : ViewModel() {
  val budgetNavEntryContributors: ImmutableSet<BudgetNavEntryContributor> =
    contributors.toImmutableSet()

  val navGridOrder: StateFlow<ImmutableList<BudgetTab>> =
    prefs.navGridOrder
      .asFlow()
      .map { reconcileNavGridOrder(it) }
      .stateIn(viewModelScope, SharingStarted.Eagerly, DEFAULT_NAV_GRID_ORDER)
}
