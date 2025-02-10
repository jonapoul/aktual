package actual.db.entity

import actual.budget.model.BalanceType
import actual.budget.model.ConditionOperator
import actual.budget.model.CustomReportMode
import actual.budget.model.DateRangeType
import actual.budget.model.GraphType
import actual.budget.model.GroupBy
import actual.budget.model.ReportDate
import actual.budget.model.SortBy
import actual.db.model.Condition
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_reports")
data class CustomReportEntity(
  @PrimaryKey @ColumnInfo("id") val id: String,
  @ColumnInfo("name") val name: String?,
  @ColumnInfo("start_date") val startDate: ReportDate?,
  @ColumnInfo("end_date") val endDate: ReportDate?,
  @ColumnInfo("date_static") val dateStatic: Boolean = false,
  @ColumnInfo("date_range") val dateRange: DateRangeType,
  @ColumnInfo("mode") val mode: CustomReportMode = CustomReportMode.Total,
  @ColumnInfo("group_by") val groupBy: GroupBy = GroupBy.Category,
  @ColumnInfo("balance_type") val balanceType: BalanceType = BalanceType.Expense,
  @ColumnInfo("show_empty") val showEmpty: Boolean = false,
  @ColumnInfo("show_offbudget") val showOffBudget: Boolean = false,
  @ColumnInfo("show_hidden") val showHidden: Boolean = false,
  @ColumnInfo("show_uncategorized") val showUncategorized: Boolean = false,
  @ColumnInfo("selected_categories") val selectedCategories: String?,
  @ColumnInfo("graph_type") val graphType: GraphType = GraphType.BarGraph,
  @ColumnInfo("conditions") val conditions: List<Condition>,
  @ColumnInfo("conditions_op") val conditionsOp: ConditionOperator? = ConditionOperator.And,
  @ColumnInfo("metadata") val metadata: String?,
  @ColumnInfo("interval") val interval: String? = "Monthly",
  @ColumnInfo("color_scheme") val colorScheme: String?,
  @ColumnInfo("tombstone") val tombstone: Boolean = false,
  @ColumnInfo("include_current") val includeCurrent: Boolean? = false,
  @ColumnInfo("sort_by") val sortBy: SortBy? = SortBy.Desc,
)
