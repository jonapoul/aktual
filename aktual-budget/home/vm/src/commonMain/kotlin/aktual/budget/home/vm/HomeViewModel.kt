package aktual.budget.home.vm

import aktual.budget.BudgetLocalPreferences
import aktual.budget.home.domain.AccountsSummary
import aktual.budget.home.domain.AccountsSummaryLoader
import aktual.budget.home.domain.UpcomingSchedules
import aktual.budget.home.domain.UpcomingSchedulesLoader
import aktual.budget.model.DbMetadata
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class HomeViewModel(
  localPreferences: BudgetLocalPreferences,
  accountsSummaryLoader: AccountsSummaryLoader,
  upcomingSchedulesLoader: UpcomingSchedulesLoader,
) : ViewModel() {
  val state: StateFlow<HomeState> =
    viewModelScope.launchMolecule(Immediate) {
      val budgetNameFlow = remember { localPreferences.observe(DbMetadata.BudgetName) }
      val budgetName by
        budgetNameFlow.collectAsState(initial = localPreferences[DbMetadata.BudgetName])

      val accountsFlow = remember { accountsSummaryLoader.observe().map { it.toCardState() } }
      val accounts by accountsFlow.collectAsState(initial = Loading)

      val upcomingFlow = remember { upcomingSchedulesLoader.observe().map { it.toCardState() } }
      val upcoming by upcomingFlow.collectAsState(initial = Loading)

      HomeState(budgetName = budgetName, upcoming = upcoming, accounts = accounts)
    }

  private fun UpcomingSchedules.toCardState(): UpcomingCardState =
    if (schedules.isEmpty()) {
      Empty
    } else {
      UpcomingCardState.Loaded(
        length = length,
        today = today,
        schedules = schedules.take(MAX_UPCOMING_ROWS).toImmutableList(),
        hiddenCount = (schedules.size - MAX_UPCOMING_ROWS).coerceAtLeast(0),
        total = total,
      )
    }

  // Closed accounts aren't shown on the card
  private fun AccountsSummary.toCardState(): AccountsCardState =
    if (onBudget.accounts.isEmpty() && offBudget.accounts.isEmpty()) {
      Empty
    } else {
      AccountsCardState.Loaded(this)
    }
}

private const val MAX_UPCOMING_ROWS = 5
