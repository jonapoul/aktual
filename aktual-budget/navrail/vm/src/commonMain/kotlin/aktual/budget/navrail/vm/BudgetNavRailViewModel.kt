package aktual.budget.navrail.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.model.DbMetadata
import aktual.core.model.ServerUrl
import aktual.core.nav.BudgetNavEntryContributor
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.StateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class BudgetNavRailViewModel(
  contributors: Set<BudgetNavEntryContributor>,
  localPreferences: BudgetLocalPreferences,
  serverUrl: ServerUrl,
) : ViewModel() {
  val budgetNavEntryContributors: ImmutableSet<BudgetNavEntryContributor> =
    contributors.toImmutableSet()

  val headerState: StateFlow<DrawerHeaderState> =
    viewModelScope.launchMolecule(Immediate) {
      val budgetNameFlow = remember { localPreferences.observe(DbMetadata.BudgetName) }
      val budgetName by
        budgetNameFlow.collectAsState(initial = localPreferences[DbMetadata.BudgetName])
      DrawerHeaderState(budgetName = budgetName, serverHost = serverUrl.baseUrl)
    }
}
