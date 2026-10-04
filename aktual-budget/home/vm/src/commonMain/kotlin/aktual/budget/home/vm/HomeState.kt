package aktual.budget.home.vm

import aktual.budget.home.domain.AccountsSummary
import aktual.budget.model.Amount
import aktual.budget.model.UpcomingLength
import aktual.budget.schedules.domain.Schedule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable
data class HomeState(
  val budgetName: String?,
  val upcoming: UpcomingCardState = Loading,
  val accounts: AccountsCardState = Loading,
)

@Immutable
sealed interface UpcomingCardState {
  data object Loading : UpcomingCardState

  data object Empty : UpcomingCardState

  // total covers the hidden schedules too
  data class Loaded(
    val length: UpcomingLength,
    val today: LocalDate,
    val schedules: ImmutableList<Schedule>,
    val hiddenCount: Int,
    val total: Amount,
  ) : UpcomingCardState
}

@Immutable
sealed interface AccountsCardState {
  data object Loading : AccountsCardState

  data object Empty : AccountsCardState

  data class Loaded(val summary: AccountsSummary) : AccountsCardState
}
