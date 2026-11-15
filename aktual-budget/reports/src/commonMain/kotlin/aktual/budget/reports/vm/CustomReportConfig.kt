package aktual.budget.reports.vm

import aktual.budget.model.BalanceType
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.CustomReportMode
import aktual.budget.model.DateRangeType
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.SelectedCategory
import aktual.budget.model.SortBy
import aktual.budget.model.TagId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

/** A row of the `custom_reports` table, with defaults filled in and unreadable values rejected */
@Immutable
data class CustomReportConfig(
  val mode: CustomReportMode,
  val groupBy: GroupBy,
  val balanceType: BalanceType,
  val graphType: GraphType,
  val interval: Interval,
  val dates: CustomReportDates,
  val showEmpty: Boolean,
  val showOffBudget: Boolean,
  val showHidden: Boolean,
  val showUncategorized: Boolean,
  val selectedCategories: ImmutableList<SelectedCategory>,
  val conditions: ImmutableList<Condition>,
  val conditionsOp: ConditionOp,
  val sortBy: SortBy,
  val trimIntervals: Boolean,
  val showTrendLines: Boolean,
  val colorScheme: String?,
  // Only used when split by tag
  val tagScope: CustomTagScope = All,
) {
  companion object {
    // packages/desktop-client/src/components/reports/ReportOptions.ts defaultReport
    val Default =
      CustomReportConfig(
        mode = Total,
        groupBy = Category,
        balanceType = Payment,
        graphType = BarGraph,
        interval = Monthly,
        dates = CustomReportDates.Live(Last6Months, includeCurrent = true),
        showEmpty = false,
        showOffBudget = false,
        showHidden = true,
        showUncategorized = true,
        selectedCategories = persistentListOf(),
        conditions = persistentListOf(),
        conditionsOp = And,
        sortBy = Desc,
        trimIntervals = false,
        showTrendLines = false,
        colorScheme = null,
      )
  }
}

// CustomReportTagScope: which tags a report split by tag buckets transactions by
@Immutable
sealed interface CustomTagScope {
  data object All : CustomTagScope

  data class Selected(val tagIds: ImmutableList<TagId>) : CustomTagScope
}

@Immutable
sealed interface CustomReportDates {
  // Moves with the current date, see resolveCustomReportRange in reports:vm
  data class Live(val type: DateRangeType, val includeCurrent: Boolean) : CustomReportDates

  data class Static(val start: LocalDate, val end: LocalDate) : CustomReportDates
}
