package aktual.budget.reports.vm

import aktual.budget.model.Amount
import aktual.budget.model.ScheduleId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

// The schedules behind a balance forecast. Opening balance + recorded net + scheduled net = ending
// balance
@Immutable
data class ForecastContributors(
  val openingBalance: Amount,
  val recordedNet: Amount,
  val scheduledNet: Amount,
  val endingBalance: Amount,
  // Largest first
  val inflows: ImmutableList<ForecastContributor>,
  val outflows: ImmutableList<ForecastContributor>,
  // Transfer occurrences between two included accounts, which cancel out
  val internalTransferCount: Int,
)

@Immutable
data class ForecastContributor(
  val id: ScheduleId,
  val name: String?,
  val amount: Amount,
  val count: Int,
  val isTransfer: Boolean,
  val isAccountless: Boolean,
)
