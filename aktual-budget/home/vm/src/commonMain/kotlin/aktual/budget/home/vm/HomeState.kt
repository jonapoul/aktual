package aktual.budget.home.vm

import aktual.budget.home.domain.AccountsSummary
import androidx.compose.runtime.Immutable

@Immutable data class HomeState(val budgetName: String?, val accounts: AccountsCardState = Loading)

@Immutable
sealed interface AccountsCardState {
  data object Loading : AccountsCardState

  data object Empty : AccountsCardState

  data class Loaded(val summary: AccountsSummary) : AccountsCardState
}
