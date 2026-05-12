package aktual.budget.navrail.vm

import aktual.core.nav.BudgetNavEntryContributor
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class BudgetNavRailViewModel(contributors: Set<BudgetNavEntryContributor>) : ViewModel() {
  val budgetNavEntryContributors: ImmutableSet<BudgetNavEntryContributor> =
    contributors.toImmutableSet()
}
