package aktual.budget.navrail.vm

import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavGraph
import aktual.budget.model.BudgetId
import aktual.core.model.Token
import aktual.prefs.AppPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.launch

@AssistedInject
class BudgetNavRailViewModel(
  @Assisted val token: Token,
  @Assisted val budgetId: BudgetId,
  factory: BudgetNavGraph.Factory,
  private val appPreferences: AppPreferences,
) : ViewModel() {
  val budgetNavEntryContributors: ImmutableSet<BudgetNavEntryContributor> =
    factory.createBudgetNavGraph().budgetNavEntryContributors.toImmutableSet()

  init {
    viewModelScope.launch { appPreferences.lastOpenedBudgetId.set(budgetId) }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(token: Token, budgetId: BudgetId): BudgetNavRailViewModel
  }
}
