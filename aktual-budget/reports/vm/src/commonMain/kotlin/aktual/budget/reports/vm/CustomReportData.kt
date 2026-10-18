package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BalanceType
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.TagId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

/** One row of a custom report: a category, payee, etc. that the report's amounts are grouped by */
@Immutable
sealed interface CustomGroup {
  data class Category(val id: CategoryId, val name: String) : CustomGroup

  data class Group(val id: CategoryGroupId, val name: String) : CustomGroup

  data class Payee(val id: PayeeId, val name: String) : CustomGroup

  data class Account(val id: AccountId, val name: String) : CustomGroup

  /** On budget, with no category and not a transfer */
  data object Uncategorized : CustomGroup

  /** On budget, with no category and a transfer payee */
  data object Transfers : CustomGroup

  /** Anything in an off-budget account, categorised or not */
  data object OffBudget : CustomGroup

  /** The category group holding [Uncategorized], [Transfers] and [OffBudget] */
  data object UncategorizedAndOffBudget : CustomGroup

  /**
   * Transactions tagged with exactly these of the report's tags, sorted by name. Tags outside the
   * report's scope don't count
   */
  data class TagSet(val ids: ImmutableList<TagId>, val names: ImmutableList<String>) : CustomGroup

  /** Transactions with none of the report's tags */
  data object Untagged : CustomGroup
}

/**
 * Upstream's balanceTypeOp: which of [CustomTotals] a report shows. Budgeted reports show [Total],
 * since upstream's totalBudgeted always mirrors totalTotals.
 */
enum class CustomMetric {
  Assets,
  Debts,
  NetAssets,
  NetDebts,
  Total,
}

// packages/desktop-client/src/components/reports/ReportOptions.ts balanceTypeOptions. Expense is
// from older reports, which upstream no longer maps, so it falls back to totalDebts like an unset
// balance type does
val BalanceType.metric: CustomMetric
  get() =
    when (this) {
      Payment,
      Expense,
      Unknown -> Debts
      Deposit -> Assets
      Net,
      Budgeted -> Total
      NetPayment -> NetDebts
      NetDeposit -> NetAssets
    }

@Immutable
data class CustomTotals(
  val assets: Amount,
  val debts: Amount,
  val netAssets: Amount,
  val netDebts: Amount,
  val total: Amount,
) {
  operator fun get(metric: CustomMetric): Amount =
    when (metric) {
      Assets -> assets
      Debts -> debts
      NetAssets -> netAssets
      NetDebts -> netDebts
      Total -> total
    }

  companion object {
    val Zero = CustomTotals(Amount.Zero, Amount.Zero, Amount.Zero, Amount.Zero, Amount.Zero)
  }
}

/**
 * One interval of a report. [start] is where the day, week, month or year begins, for labelling it.
 * [range] is the dates it covers, which for the first and last intervals is cut to the report's
 * dates.
 */
@Immutable data class CustomInterval(val start: LocalDate, val range: LocalDateRange)

/**
 * The whole report's totals for one interval. [stacked] has each group's amount for the report's
 * metric, as a stacked chart draws it: debts are made positive.
 */
@Immutable
data class CustomIntervalTotals(
  val interval: CustomInterval,
  val totals: CustomTotals,
  val stacked: ImmutableMap<CustomGroup, Amount>,
)

/** One group's totals for one interval. [change] is how much its total moved since the last one */
@Immutable
data class CustomGroupIntervalTotals(
  val interval: CustomInterval,
  val totals: CustomTotals,
  val change: Amount,
)

/** [categories] is only filled in for category groups, in [CustomData.categoryGroups] */
@Immutable
data class CustomGroupTotals(
  val group: CustomGroup,
  val totals: CustomTotals,
  val intervals: ImmutableList<CustomGroupIntervalTotals>,
  val categories: ImmutableList<CustomGroupTotals>,
)

/**
 * A straight line fitted through one group's amounts, as a stacked chart draws them. [start] is its
 * value at the first interval and [end] at the last.
 */
@Immutable data class CustomTrend(val start: Amount, val end: Amount)
