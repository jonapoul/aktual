package aktual.budget.reports.vm

import aktual.budget.model.Amount
import aktual.budget.model.ScheduleId
import androidx.compose.runtime.Immutable
import kotlin.math.absoluteValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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

private data class ContributorTotal(
  val id: ScheduleId,
  val name: String?,
  val amount: Long,
  val count: Int,
  val isTransfer: Boolean,
  val isAccountless: Boolean,
)

// packages/loot-core/src/shared/forecast-contributors.ts summarizeForecastContributors(). included
// holds the occurrences the chart counted, already filtered to its accounts and range.
internal fun summarizeForecastContributors(
  included: List<ForecastOccurrence>,
  openingBalance: Long,
  recordedNet: Long,
  endingBalance: Long,
): ForecastContributors {
  // Net the legs of a transfer before ranking, and count each occurrence once
  val occurrences =
    included
      .groupBy { it.scheduleId to it.date }
      .values
      .map { legs ->
        val first = legs.first()
        ContributorTotal(
          id = first.scheduleId,
          name = first.scheduleName,
          amount = legs.sumOf { it.amount },
          count = 1,
          isTransfer = legs.any { it.isTransferSchedule },
          isAccountless = legs.any { it.account == null },
        )
      }

  val internalTransferCount = occurrences.count { it.amount == 0L && it.isTransfer }

  // A schedule could change direction, so keep both sides visible
  val contributors =
    occurrences
      .filter { it.amount != 0L }
      .groupBy { it.id to (it.amount > 0) }
      .values
      .map { group ->
        group
          .first()
          .copy(
            amount = group.sumOf { it.amount },
            count = group.size,
            isTransfer = group.any { it.isTransfer },
            isAccountless = group.any { it.isAccountless },
          )
      }
      .sortedWith(
        compareByDescending<ContributorTotal> { it.amount.absoluteValue }
          .thenBy { it.name.orEmpty() }
          .thenBy { it.id.value },
      )

  return ForecastContributors(
    openingBalance = Amount(openingBalance),
    recordedNet = Amount(recordedNet),
    scheduledNet = Amount(contributors.sumOf { it.amount }),
    endingBalance = Amount(endingBalance),
    inflows = contributors.filter { it.amount > 0 }.map { it.toContributor() }.toImmutableList(),
    outflows = contributors.filter { it.amount < 0 }.map { it.toContributor() }.toImmutableList(),
    internalTransferCount = internalTransferCount,
  )
}

private fun ContributorTotal.toContributor() =
  ForecastContributor(
    id = id,
    name = name,
    amount = Amount(amount),
    count = count,
    isTransfer = isTransfer,
    isAccountless = isAccountless,
  )
