package aktual.budget.reports.vm

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth

// The settings a budget analysis report can edit, with upstream's defaults filled in. Upstream's
// report is always monthly, so its interval isn't one of them
@Immutable
data class BudgetAnalysisSettings(
  val timeFrame: TimeFrame,
  val conditions: ImmutableList<Condition>,
  val conditionsOp: ConditionOp,
  val graphType: GraphType,
  val showBalance: Boolean,
  val balanceOnly: Boolean,
  val showHiddenCategories: Boolean,
)

// The fields its filters can use, since it only filters categories
val BUDGET_ANALYSIS_FIELDS = persistentListOf<Field>(Category, CategoryGroup)

internal fun BudgetAnalysisReportMeta.toSettings(current: YearMonth) =
  BudgetAnalysisSettings(
    timeFrame = timeFrame ?: defaultTimeFrame(current),
    conditions = conditions.orEmpty().toImmutableList(),
    conditionsOp = conditionsOp ?: And,
    graphType = graphType?.takeIf { it != Unknown } ?: Bar,
    showBalance = showBalance != false,
    balanceOnly = balanceOnly == true,
    showHiddenCategories = showHiddenCategories == true,
  )

internal fun BudgetAnalysisReportMeta.withSettings(settings: BudgetAnalysisSettings) =
  copy(
    timeFrame = settings.timeFrame,
    conditions = settings.conditions,
    conditionsOp = settings.conditionsOp,
    graphType = settings.graphType,
    showBalance = settings.showBalance,
    balanceOnly = settings.balanceOnly,
    showHiddenCategories = settings.showHiddenCategories,
  )
