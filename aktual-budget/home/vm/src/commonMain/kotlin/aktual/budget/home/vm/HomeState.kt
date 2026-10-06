package aktual.budget.home.vm

import aktual.budget.home.domain.AccountsSummary
import aktual.budget.home.domain.OverspentCategory
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import aktual.budget.model.UpcomingLength
import aktual.budget.schedules.domain.Schedule
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

@Immutable
data class HomeState(
  val budgetName: String?,
  val thisMonth: ThisMonthCardState = Loading,
  val attention: AttentionCardState = Loading,
  val upcoming: UpcomingCardState = Loading,
  val accounts: AccountsCardState = Loading,
)

@Immutable
sealed interface ThisMonthCardState {
  data object Loading : ThisMonthCardState

  sealed interface Loaded : ThisMonthCardState {
    val month: YearMonth
    val daysLeft: Int

    // Positive when money has gone out
    val spent: Amount
    val budgeted: Amount
  }

  data class Envelope(
    override val month: YearMonth,
    override val daysLeft: Int,
    override val spent: Amount,
    override val budgeted: Amount,
    val toBudget: Amount,
  ) : Loaded

  data class Tracking(
    override val month: YearMonth,
    override val daysLeft: Int,
    override val spent: Amount,
    override val budgeted: Amount,
    val income: Amount,
    val incomeBudgeted: Amount,
  ) : Loaded {
    val remaining: Amount
      get() = budgeted - spent
  }
}

@Immutable
sealed interface AttentionCardState {
  data object Loading : AttentionCardState

  data object Empty : AttentionCardState

  data class Loaded(val items: ImmutableList<AttentionItem>) : AttentionCardState
}

@Immutable
sealed interface AttentionItem {
  data class SyncFailed(val account: AccountId, val name: String, val status: BankSyncStatus) :
    AttentionItem

  // Stands in for the individual rows when too many accounts are failing
  data class SyncFailedMany(val count: Int) : AttentionItem

  data class Uncategorised(val count: Int) : AttentionItem

  data class Overspent(val categories: ImmutableList<OverspentCategory>) : AttentionItem

  data class OverdueSchedules(val count: Int) : AttentionItem
}

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
